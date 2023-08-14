package app.myzel394.alibi.services

import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.camera.core.Camera
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.CameraController.VIDEO_CAPTURE
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.camera.view.video.ExperimentalVideo
import androidx.camera.view.video.OnVideoSavedCallback
import androidx.camera.view.video.OutputFileOptions
import androidx.camera.view.video.OutputFileResults
import androidx.compose.ui.input.key.Key.Companion.D
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File


@ExperimentalVideo class VideoRecorderService: LifecycleService() {
    lateinit var cameraController: LifecycleCameraController

    private var job = SupervisorJob()
    private var scope = CoroutineScope(Dispatchers.IO + job)

    override fun onCreate() {
        super.onCreate()

        scope.launch {
            delay(6000L)
            startRecording()
        }
    }

    fun startRecording() {

    }
}

/*
@ExperimentalVideo class VideoRecorderService: IntervalRecorderService() {
    var amplitudesAmount = 1000

    lateinit var cameraController: LifecycleCameraController
    private lateinit var preview: PreviewView

    var onError: () -> Unit = {}

    public fun bindToLifecycle(lifecycleOwner: LifecycleOwner) {
        cameraController.bindToLifecycle(lifecycleOwner)
    }

    public fun unbindLifecycle() {
        cameraController.unbind()
    }

    private fun refreshController() {
        cameraController.startRecording(
            OutputFileOptions
                .builder(File(filePath))
                .build(),
            ContextCompat.getMainExecutor(this),
            object: OnVideoSavedCallback {
                override fun onVideoSaved(outputFileResults: OutputFileResults) {
                }

                override fun onError(videoCaptureError: Int, message: String, cause: Throwable?) {
                    onError()
                }
            }
        )
    }

    override fun start() {
        cameraController = LifecycleCameraController(this)
        cameraController.setEnabledUseCases(VIDEO_CAPTURE)
        cameraController.bindToLifecycle(this)

        super.start()
    }

    override fun stop() {
        super.stop()

        cameraController.unbind()
    }

    override fun startNewCycle() {
        super.startNewCycle()

        refreshController()
    }

    override fun getAmplitudeAmount(): Int = amplitudesAmount

    override fun getAmplitude(): Int = 0
}
 */