package app.myzel394.alibi.services

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Range
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.TorchState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.db.RecordingInformation
import app.myzel394.alibi.enums.RecorderState
import app.myzel394.alibi.helpers.BatchesFolder
import app.myzel394.alibi.helpers.VideoBatchesFolder
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.properties.Delegates

const val CAMERA_CLOSE_TIMEOUT = 20000L

class VideoRecorderService :
    IntervalRecorderService<VideoRecorderService.Settings, RecordingInformation>() {
    override var batchesFolder: BatchesFolder = VideoBatchesFolder.viaInternalFolder(this)

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var activeRecording: Recording? = null

    // Used to listen and check if the camera is available
    private var _cameraAvailableListener = CompletableDeferred<Unit>()
    private var _videoFinalizerListener = CompletableDeferred<Unit>()

    // Absolute last completer that can be awaited to ensure that the camera is closed
    private var _cameraCloserListener = CompletableDeferred<Unit>()

    private lateinit var selectedCamera: CameraSelector
    private var enableAudio by Delegates.notNull<Boolean>()

    var cameraControl: CameraControl? = null
        private set

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "init") {
            selectedCamera = CameraSelector.Builder().requireLensFacing(
                intent.getIntExtra("cameraID", CameraSelector.LENS_FACING_BACK)
            ).build()
            enableAudio = intent.getBooleanExtra("enableAudio", true)
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun start() {
        super.start()

        scope.launch {
            openCamera()
        }
    }

    override suspend fun stop() {
        super.stop()

        stopActiveRecording()

        // Camera can only be closed after the recording has been finalized
        withTimeoutOrNull(CAMERA_CLOSE_TIMEOUT) {
            _videoFinalizerListener.await()
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

    @SuppressLint("MissingPermission")
    override fun startNewCycle() {
        super.startNewCycle()

        fun action() {
            activeRecording?.stop()
            val newRecording = prepareVideoRecording()

            activeRecording = newRecording.start(ContextCompat.getMainExecutor(this)) { event ->
                if (event is VideoRecordEvent.Finalize && this@VideoRecorderService.state == RecorderState.IDLE) {
                    _videoFinalizerListener.complete(Unit)
                }
            }
        }

        if (_cameraAvailableListener.isCompleted) {
            action()
        } else {
            // Race condition of `startNewCycle` being called before `invpkeOnCompletion`
            // has been called can be ignored, as the camera usually opens within 5 seconds
            // and the interval can't be set shorter than 10 seconds.
            _cameraAvailableListener.invokeOnCompletion {
                action()
            }
        }
    }


    // Runs a function in the main thread
    private fun runOnMain(callback: () -> Unit) {
        val mainHandler = ContextCompat.getMainExecutor(this)

        mainHandler.execute(callback)
    }

    private fun buildRecorder() = Recorder.Builder()
        .setQualitySelector(settings.quality)
        .apply {
            if (settings.targetVideoBitRate != null) {
                setTargetVideoEncodingBitRate(settings.targetVideoBitRate!!)
            }
        }
        .build()

    private fun buildVideoCapture(recorder: Recorder) = VideoCapture.Builder(recorder)
        .apply {
            if (settings.targetFrameRate != null) {
                setTargetFrameRate(Range(settings.targetFrameRate!!, settings.targetFrameRate!!))
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
            camera = cameraProvider!!.bindToLifecycle(
                this,
                selectedCamera,
                videoCapture
            )
            cameraControl = CameraControl(camera!!)

            _cameraAvailableListener.complete(Unit)
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
            camera = null
        }
    }

    // `resume` override not needed as `startNewCycle` is called by `IntervalRecorderService`

    private fun stopActiveRecording() {
        activeRecording?.stop()
    }

    @SuppressLint("MissingPermission")
    private fun prepareVideoRecording() =
        videoCapture!!.output
            .prepareRecording(this, settings.getOutputOptions(this))
            .run {
                if (enableAudio) {
                    return@run withAudioEnabled()
                }

                this
            }

    override fun getRecordingInformation(): RecordingInformation = RecordingInformation(
        folderPath = batchesFolder.exportFolderForSettings(),
        recordingStart = recordingStart,
        maxDuration = settings.maxDuration,
        fileExtension = settings.fileExtension,
        intervalDuration = settings.intervalDuration,
        type = RecordingInformation.Type.VIDEO,
    )

    // TODO: Save camera selector as it doesn't make sense to change the camera midway
    data class Settings(
        override val maxDuration: Long,
        override val intervalDuration: Long,
        val folder: String? = null,
        val targetVideoBitRate: Int? = null,
        val targetFrameRate: Int? = null,
        val quality: QualitySelector = QualitySelector.from(Quality.HIGHEST),
    ) : IntervalRecorderService.Settings(
        maxDuration = maxDuration,
        intervalDuration = intervalDuration
    ) {
        val fileExtension
            get() = "mp4"

        fun getOutputOptions(video: VideoRecorderService): FileOutputOptions {
            val fileName = "${video.counter}.$fileExtension"
            val file = video.batchesFolder.getInternalFolder().resolve(fileName).apply {
                createNewFile()
            }

            return FileOutputOptions.Builder(file).build()
        }

        companion object {
            fun from(appSettings: AppSettings) = Settings(
                maxDuration = appSettings.maxDuration,
                intervalDuration = appSettings.intervalDuration,
                folder = appSettings.saveFolder,
                targetVideoBitRate = appSettings.videoRecorderSettings.targetedVideoBitRate,
                targetFrameRate = appSettings.videoRecorderSettings.targetFrameRate,
                quality = appSettings.videoRecorderSettings.getQualitySelector()
                    ?: QualitySelector.from(Quality.HIGHEST),
            )
        }
    }

    class CameraControl(
        private val camera: Camera,
    ) {
        fun enableTorch() {
            camera.cameraControl.enableTorch(true)
        }

        fun disableTorch() {
            camera.cameraControl.enableTorch(false)
        }

        fun isTorchEnabled(): Boolean {
            return camera.cameraInfo.torchState.value == TorchState.ON
        }

        fun hasTorchAvailable() = camera.cameraInfo.hasFlashUnit()
    }
}
