package app.myzel394.alibi.services

import androidx.camera.view.CameraController.VIDEO_CAPTURE
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.video.ExperimentalVideo
import androidx.camera.view.video.OnVideoSavedCallback
import androidx.camera.view.video.OutputFileOptions
import androidx.camera.view.video.OutputFileResults
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File

@ExperimentalVideo class VideoRecorderService: IntervalRecorderService() {
    var amplitudesAmount = 1000

    val cameraController = LifecycleCameraController(this)

    var onError: () -> Unit = {}

    public fun bindToLifecycle(lifecycleOwner: LifecycleOwner) {
        cameraController.bindToLifecycle(lifecycleOwner)
    }

    public fun unbindLifecycle() {
        cameraController.unbind()
    }

    private fun refreshController() {
        if (cameraController.isRecording) {
            cameraController.stopRecording()
        }

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
        cameraController.setEnabledUseCases(VIDEO_CAPTURE)
        super.start()
    }

    override fun startNewCycle() {
        super.startNewCycle()

        refreshController()
    }

    override fun getAmplitudeAmount(): Int = amplitudesAmount

    override fun getAmplitude(): Int = 0
}