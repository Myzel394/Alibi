package app.myzel394.alibi.services

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Range
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.TorchState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileDescriptorOutputOptions
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import app.myzel394.alibi.NotificationHelper
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.db.RecordingInformation
import app.myzel394.alibi.enums.RecorderState
import app.myzel394.alibi.helpers.BatchesFolder
import app.myzel394.alibi.helpers.VideoBatchesFolder
import app.myzel394.alibi.ui.SUPPORTS_SAVING_VIDEOS_IN_CUSTOM_FOLDERS
import app.myzel394.alibi.ui.SUPPORTS_SCOPED_STORAGE
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.properties.Delegates

class VideoRecorderService :
    IntervalRecorderService<RecordingInformation, VideoBatchesFolder>() {
    override var batchesFolder = VideoBatchesFolder.viaInternalFolder(this)

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var activeRecording: Recording? = null

    // Used to listen and check if the camera is available
    private var _cameraAvailableListener = CompletableDeferred<Unit>()
    private lateinit var _videoFinalizerListener: CompletableDeferred<Unit>;

    // Absolute last completer that can be awaited to ensure that the camera is closed
    private var _cameraCloserListener = CompletableDeferred<Unit>()

    private lateinit var selectedCamera: CameraSelector
    var lensFacing: Int = CameraSelector.LENS_FACING_BACK
        private set
    var cameraSelection: app.myzel394.alibi.db.CameraSelection? = null
        private set
    private var enableAudio by Delegates.notNull<Boolean>()

    var onCameraControlAvailable = {}

    var cameraControl: CameraControl? = null
        private set

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_START_VIDEO) {
            if (!app.myzel394.alibi.recording.ShortcutRequest.matches(intent.getStringExtra("shortcutToken"))) {
                if (state == RecorderState.IDLE) stopSelf(startId)
                return android.app.Service.START_NOT_STICKY
            }
            shortcutRequested = true
            app.myzel394.alibi.recording.ShortcutRequest.clear()
        }
        if (intent?.action == "init" || intent?.action == ACTION_START_VIDEO) {
            try {
                lensFacing = intent.getIntExtra("lensFacing", CameraSelector.LENS_FACING_BACK)
                cameraSelection = intent.getStringExtra("logicalCameraId")?.let {
                    app.myzel394.alibi.db.CameraSelection(it, intent.getStringExtra("physicalCameraId"))
                }
                cameraSelection?.let { selected ->
                    require(app.myzel394.alibi.ui.utils.CameraInfo.queryAvailableCameras(this).any {
                        it.selection == selected && it.lens.androidValue == lensFacing
                    }) { "Selected camera is unavailable" }
                }
                selectedCamera = app.myzel394.alibi.ui.utils.CameraInfo.selector(lensFacing, cameraSelection)
                enableAudio = intent.getBooleanExtra("enableAudio", true)
            } catch (error: Exception) {
                android.util.Log.e("AlibiShortcut", "Camera selection unavailable", error)
                reportStartFailure()
                return android.app.Service.START_NOT_STICKY
            }
        }

        if (intent?.action == ACTION_START_VIDEO && state == RecorderState.IDLE) {
            shortcutRequested = true
            try {
                settings = kotlinx.serialization.json.Json.decodeFromString(
                    AppSettings.serializer(), intent.getStringExtra("settings")!!)
                batchesFolder = VideoBatchesFolder.importFromFolder(
                    settings.saveFolder ?: app.myzel394.alibi.ui.RECORDER_INTERNAL_SELECTED_VALUE, this)
                batchesFolder.initFolders()
                app.myzel394.alibi.recording.RecordingLibrary.archiveFragments(batchesFolder)
                startRecording()
            } catch (error: Exception) {
                android.util.Log.e("AlibiShortcut", "Foreground camera initialization rejected", error)
                reportStartFailure()
            }
            return android.app.Service.START_NOT_STICKY
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun start() {
        super.start()

        scope.launch {
            try { openCamera() } catch (error: Exception) {
                withContext(Dispatchers.Main) { reportStartFailure() }
            }
        }
    }

    override suspend fun stop() {
        super.stop()

        stopActiveRecording()

        // Camera can only be closed after the recording has been finalized
        withTimeoutOrNull(CAMERA_CLOSE_TIMEOUT) {
            if (::_videoFinalizerListener.isInitialized) _videoFinalizerListener.await()
        }

        closeCamera()

        withTimeoutOrNull(CAMERA_CLOSE_TIMEOUT) {
            _cameraCloserListener.await()
        }
    }

    override fun pause() {
        super.pause()

        stopActiveRecording()
    }

    override fun startForegroundService() {
        ServiceCompat.startForeground(
            this,
            NotificationHelper.RECORDER_CHANNEL_NOTIFICATION_ID,
            getNotificationHelper().buildStartingNotification(),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (enableAudio)
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
                else
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
            } else {
                0
            },
        )
    }

    @SuppressLint("MissingPermission")
    override fun startNewCycle() {
        super.startNewCycle()

        fun action() {
            if (state != RecorderState.RECORDING) return
            stopActiveRecording()
            val newRecording = prepareVideoRecording()

            val segmentFinalizer = CompletableDeferred<Unit>()
            _videoFinalizerListener = segmentFinalizer

            activeRecording = newRecording.start(ContextCompat.getMainExecutor(this)) { event ->
                if (event is VideoRecordEvent.Start) {
                    confirmShortcutStart()
                }
                if (event is VideoRecordEvent.Finalize && event.hasError() && state == RecorderState.RECORDING) reportStartFailure()
                if (event is VideoRecordEvent.Finalize) segmentFinalizer.complete(Unit)
            }
        }

        if (_cameraAvailableListener.isCompleted) {
            action()
        } else {
            // Race condition of `startNewCycle` being called before `invokeOnCompletion`
            // has been called can be ignored, as the camera usually opens within 5 seconds
            // and the interval can't be set shorter than 10 seconds.
            _cameraAvailableListener.invokeOnCompletion {
                action()
            }
        }
    }


    override fun onDestroy() {
        stopActiveRecording()
        cameraProvider?.unbindAll()
        job.cancel()
        super.onDestroy()
    }

    // Runs a function in the main thread
    private fun runOnMain(callback: () -> Unit) {
        val mainHandler = ContextCompat.getMainExecutor(this)

        mainHandler.execute(callback)
    }

    private fun qualitySelectorForCamera(): QualitySelector {
        val requested = settings.videoRecorderSettings.getQuality() ?: Quality.HIGHEST
        val physical = cameraSelection?.physicalId
        if (physical != null && Build.VERSION.SDK_INT >= 28) {
            val sizes = runCatching {
                getSystemService(android.hardware.camera2.CameraManager::class.java).getCameraCharacteristics(physical)
                    .get(android.hardware.camera2.CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    ?.getOutputSizes(android.media.MediaRecorder::class.java)
            }.getOrNull()
            val maxHeight = sizes?.maxOfOrNull { minOf(it.width, it.height) } ?: 1080
            val ordered = listOf(Quality.UHD to 2160, Quality.FHD to 1080, Quality.HD to 720, Quality.SD to 480)
                .filter { it.second <= maxHeight }.map { it.first }.ifEmpty { listOf(Quality.SD) }
            val choices = when (requested) {
                Quality.LOWEST -> ordered.reversed()
                Quality.HIGHEST -> ordered
                else -> if (requested in ordered) listOf(requested) + ordered.filter { it != requested } else ordered
            }
            return QualitySelector.fromOrderedList(choices, androidx.camera.video.FallbackStrategy.lowerQualityOrHigherThan(Quality.SD))
        }
        return QualitySelector.from(requested, androidx.camera.video.FallbackStrategy.lowerQualityOrHigherThan(Quality.SD))
    }

    private fun buildRecorder() = Recorder.Builder()
        .setQualitySelector(
            qualitySelectorForCamera()
        )
        .apply {
            if (settings.videoRecorderSettings.targetedVideoBitRate != null) {
                setTargetVideoEncodingBitRate(settings.videoRecorderSettings.targetedVideoBitRate!!)
            }
        }
        .build()

    @androidx.annotation.OptIn(androidx.camera.camera2.interop.ExperimentalCamera2Interop::class)
    private fun buildVideoCapture(recorder: Recorder) = VideoCapture.Builder(recorder)
        .apply {
            cameraSelection?.physicalId?.let { physical ->
                if (Build.VERSION.SDK_INT >= 28) androidx.camera.camera2.interop.Camera2Interop.Extender(this).setPhysicalCameraId(physical)
            }
            val frameRate = settings.videoRecorderSettings.targetFrameRate
            if (frameRate != null) {
                setTargetFrameRate(Range(frameRate, frameRate))
            }
        }
        .build()

    // Open the camera.
    // Used to open it for a longer time, shouldn't be called when pausing / resuming.
    // This should only be called when starting a recording.
    private suspend fun openCamera() {
        cameraProvider = withContext(Dispatchers.IO) {
            ProcessCameraProvider.getInstance(this@VideoRecorderService).get()
        }

        val recorder = buildRecorder()
        videoCapture = buildVideoCapture(recorder)

        runOnMain {
            try {
                camera = cameraProvider!!.bindToLifecycle(
                    this,
                    selectedCamera,
                    videoCapture
                )

                cameraControl = CameraControl(camera!!).also {
                    it.init()
                }
                onCameraControlAvailable()

                _cameraAvailableListener.complete(Unit)
            } catch (error: RuntimeException) {
                reportStartFailure()
            }
        }
    }

    // Close the camera
    // Used to close it finally, shouldn't be called when pausing / resuming.
    // This should only be called after recording has finished.
    private fun closeCamera() {
        runOnMain {
            runCatching {
                cameraProvider?.unbindAll()
            }
            _cameraCloserListener.complete(Unit)

            // Doesn't need to run on main thread, but
            // if it runs outside `runOnMain`, `cameraProvider` is already null
            // before it's unbound
            cameraProvider = null
            videoCapture = null
            cameraControl = null
            camera = null
        }
    }

    // `resume` override not needed as `startNewCycle` is called by `IntervalRecorderService`

    private fun stopActiveRecording() {
        runCatching {
            activeRecording?.stop()
        }
    }

    private fun getNameForMediaFile() =
        "${batchesFolder.mediaPrefix}$counter.${settings.videoRecorderSettings.fileExtension}"

    @SuppressLint("MissingPermission", "NewApi")
    private fun prepareVideoRecording() =
        videoCapture!!.output
            .let {
                if (batchesFolder.type == BatchesFolder.BatchType.CUSTOM && SUPPORTS_SAVING_VIDEOS_IN_CUSTOM_FOLDERS) {
                    it.prepareRecording(
                        this,
                        FileDescriptorOutputOptions.Builder(
                            batchesFolder.asCustomGetParcelFileDescriptor(
                                counter,
                                settings.videoRecorderSettings.fileExtension
                            )
                        ).build()
                    )
                } else if (batchesFolder.type == BatchesFolder.BatchType.MEDIA) {
                    if (SUPPORTS_SCOPED_STORAGE) {
                        val name = getNameForMediaFile()

                        it.prepareRecording(
                            this,
                            MediaStoreOutputOptions
                                .Builder(
                                    contentResolver,
                                    batchesFolder.scopedMediaContentUri,
                                )
                                .setContentValues(
                                    batchesFolder.asMediaGetScopedStorageContentValues(
                                        name
                                    )
                                )
                                .build()
                        )
                    } else {
                        val name = getNameForMediaFile()

                        it.prepareRecording(
                            this,
                            FileOutputOptions
                                .Builder(batchesFolder.asMediaGetLegacyFile(name))
                                .build()
                        )
                    }
                } else {
                    it.prepareRecording(
                        this,
                        FileOutputOptions.Builder(
                            batchesFolder.asInternalGetFile(
                                counter,
                                settings.videoRecorderSettings.fileExtension
                            ).apply {
                                createNewFile()
                            }
                        ).build()
                    )
                }
            }
            .run {
                if (enableAudio) {
                    return@run withAudioEnabled()
                }

                this
            }

    override fun getRecordingInformation() =
        RecordingInformation(
            folderPath = batchesFolder.exportFolderForSettings(),
            recordingStart = recordingStart,
            maxDuration = settings.maxDuration,
            batchesAmount = batchesFolder.getBatchesForFFmpeg().size,
            fileExtension = settings.videoRecorderSettings.fileExtension,
            intervalDuration = settings.intervalDuration,
            type = RecordingInformation.Type.VIDEO,
        )

    companion object {
        const val CAMERA_CLOSE_TIMEOUT = 20000L
    }

    class CameraControl(
        val camera: Camera,
        // Save state for optimistic updates
        var torchEnabled: Boolean = false,
    ) {
        fun init() {
            torchEnabled = camera.cameraInfo.torchState.value == TorchState.ON
        }

        fun enableTorch() {
            torchEnabled = true
            camera.cameraControl.enableTorch(true)
        }

        fun disableTorch() {
            torchEnabled = false
            camera.cameraControl.enableTorch(false)
        }

        fun isHardwareTorchReallyEnabled(): Boolean {
            return camera.cameraInfo.torchState.value == TorchState.ON
        }

        fun hasTorchAvailable() = camera.cameraInfo.hasFlashUnit()
    }
}
