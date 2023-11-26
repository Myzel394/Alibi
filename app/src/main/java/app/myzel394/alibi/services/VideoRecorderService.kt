package app.myzel394.alibi.services

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.pm.ServiceInfo
import android.os.Build
import android.provider.MediaStore
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoCapture.withOutput
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleService
import app.myzel394.alibi.NotificationHelper
import app.myzel394.alibi.db.RecordingInformation
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


class VideoService : IntervalRecorderService<VideoService.Settings, RecordingInformation>() {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private var cameraProvider: ProcessCameraProvider? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var activeRecording: Recording? = null

    // Used to listen and check if the camera is available
    private var _cameraAvailableListener = CompletableDeferred<Boolean>()

    // Runs a function in the main thread
    private fun runInMain(callback: () -> Unit) {
        val mainHandler = ContextCompat.getMainExecutor(this)

        mainHandler.execute(callback)
    }

    // Open the camera.
    // Used to open it for a longer time, shouldn't be called when pausing / resuming.
    // This should only be called when starting a recording.
    private suspend fun openCamera() {
        cameraProvider = withContext(Dispatchers.IO) {
            ProcessCameraProvider.getInstance(this@VideoService).get()
        }

        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
            .build()
        videoCapture = withOutput(recorder)

        runInMain {
            cameraProvider!!.bindToLifecycle(
                this,
                settings.camera,
                videoCapture
            )

            _cameraAvailableListener.complete(true)
        }
    }

    // Close the camera
    // Used to close it finally, shouldn't be called when pausing / resuming.
    // This should only be called after recording has finished.
    private fun closeCamera() {
        clearOldVideoRecording()

        runCatching {
            cameraProvider?.unbindAll()
        }

        cameraProvider = null
        videoCapture = null
    }

    override fun start() {
        super.start()

        scope.launch {
            openCamera()
        }
    }

    override fun stop() {
        super.stop()

        closeCamera()
    }

    private fun clearOldVideoRecording() {
        runCatching {
            activeRecording?.stop()
        }
    }

    @SuppressLint("MissingPermission")
    private fun prepareVideoRecording() =
        videoCapture!!.output
            .prepareRecording(this, settings.getOutputOptions(this))
            .withAudioEnabled()

    @SuppressLint("MissingPermission")
    override fun startNewCycle() {
        super.startNewCycle()

        fun action() {
            println("=======================")
            activeRecording?.stop()
            val newRecording = prepareVideoRecording()

            activeRecording = newRecording.start(ContextCompat.getMainExecutor(this), {})
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

    override fun getRecordingInformation(): RecordingInformation = RecordingInformation(
        folderPath = batchesFolder.exportFolderForSettings(),
        recordingStart = recordingStart,
        maxDuration = settings.maxDuration,
        fileExtension = settings.fileExtension,
        intervalDuration = settings.intervalDuration,
    )

    data class Settings(
        override val maxDuration: Long,
        override val intervalDuration: Long,
        val folder: String? = null,
        val camera: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    ) : IntervalRecorderService.Settings(
        maxDuration = maxDuration,
        intervalDuration = intervalDuration
    ) {
        val fileExtension
            get() = "mp4"

        fun getOutputOptions(video: VideoService): FileOutputOptions {
            val fileName = "${video.counter}.$fileExtension"
            val file = video.batchesFolder.getInternalFolder().resolve(fileName).apply {
                createNewFile()
            }

            return FileOutputOptions.Builder(file).build()
        }

        companion object {
            fun from() = Settings(
                maxDuration = 60_000,
                intervalDuration = 10_000,
            )
        }
    }
}