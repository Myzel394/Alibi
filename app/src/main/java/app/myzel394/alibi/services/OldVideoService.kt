package app.myzel394.alibi.services

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.pm.ServiceInfo
import android.os.Build
import android.provider.MediaStore
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture.withOutput
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import app.myzel394.alibi.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


class VideoService : IntervalRecorderService() {
}

class OldVideoService : LifecycleService() {
    private var job = SupervisorJob()
    private var scope = CoroutineScope(Dispatchers.IO + job)

    private var counter = 0
    private lateinit var cycleTimer: ScheduledExecutorService
    private var recording: Recording? = null

    private fun createMediaStoreOutputOptions(): MediaStoreOutputOptions {
        val name = "CameraX-recording-$counter.mp4"
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA + ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            else
                0,
        )

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider = cameraProviderFuture.get()
            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.LOWEST))
                .build()
            val videoCapture = withOutput(recorder)
            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            // Unbind use cases before rebinding
            cameraProvider?.unbindAll()
            // Bind use cases to camera
            cameraProvider?.bindToLifecycle(
                this@OldVideoService,
                cameraSelector,
                videoCapture
            )

            val options = createMediaStoreOutputOptions()

            cycleTimer = Executors.newSingleThreadScheduledExecutor().also {
                it.scheduleAtFixedRate(
                    {
                        val mainHandler = ContextCompat.getMainExecutor(this@OldVideoService)

                        mainHandler.execute {
                            runCatching {
                                recording?.stop()
                            }

                            val r =
                                videoCapture.output.prepareRecording(this@OldVideoService, options)
                                    .withAudioEnabled()

                            recording =
                                r.start(ContextCompat.getMainExecutor(this@OldVideoService), {})
                        }
                    },
                    0,
                    10_000,
                    TimeUnit.MILLISECONDS
                )
            }
        }, ContextCompat.getMainExecutor(this))
    }
}