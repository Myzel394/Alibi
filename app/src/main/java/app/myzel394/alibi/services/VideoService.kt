package app.myzel394.alibi.services

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.pm.ServiceInfo
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.os.Build
import android.provider.MediaStore
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import androidx.camera.core.CameraProvider
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.Preview.SurfaceProvider
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture.withOutput
import androidx.camera.video.VideoRecordEvent
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import app.myzel394.alibi.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class VideoService : LifecycleService() {
    private var job = SupervisorJob()
    private var scope = CoroutineScope(Dispatchers.IO + job)

    private fun createMediaStoreOutputOptions(): MediaStoreOutputOptions {
        val name = "CameraX-recording.mp4"
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, name)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM/Recorded Videos")
            }
        }
        return MediaStoreOutputOptions.Builder(
            contentResolver,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        )
            .setContentValues(contentValues)
            .build()
    }

    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate()

        val notification = NotificationCompat.Builder(
            this,
            NotificationHelper.RECORDER_CHANNEL_ID
        ).setContentTitle("Video Recorder")
            .setContentText("Recording video")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .build()

        ServiceCompat.startForeground(
            this,
            NotificationHelper.RECORDER_CHANNEL_NOTIFICATION_ID,
            notification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
            } else {
                0
            },
        )

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider = cameraProviderFuture.get()
            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                .build()
            val videoCapture = withOutput(recorder)
            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            // Unbind use cases before rebinding
            cameraProvider?.unbindAll()
            // Bind use cases to camera
            cameraProvider?.bindToLifecycle(this@VideoService, cameraSelector, videoCapture)

            val options = createMediaStoreOutputOptions()

            val recording = videoCapture.output.prepareRecording(this@VideoService, options)
                .withAudioEnabled()

            val result = recording.start(ContextCompat.getMainExecutor(this@VideoService), {})

            scope.launch {
                delay(15000)

                result.stop()

                cameraProvider.unbindAll()
                stopSelf()
            }
        }, ContextCompat.getMainExecutor(this))
    }
}