package app.myzel394.alibi.services

import android.annotation.SuppressLint
import android.app.Notification
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import app.myzel394.alibi.NotificationHelper
import app.myzel394.alibi.enums.RecorderState
import app.myzel394.alibi.ui.utils.PermissionHelper
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


abstract class RecorderService : LifecycleService() {
    private val binder = RecorderBinder()

    private var isPaused: Boolean = false
    lateinit var recordingStart: LocalDateTime
        private set
    private lateinit var recordingTimeTimer: ScheduledExecutorService
    private var notificationDetails: RecorderNotificationHelper.NotificationDetails? = null

    var state = RecorderState.IDLE
        private set

    var onStateChange: ((RecorderState) -> Unit)? = null
    var onError: () -> Unit = {}
    var onRecordingTimeChange: ((Long) -> Unit)? = null

    var recordingTime = 0L
        private set

    protected open fun start() {
        createRecordingTimeTimer()
    }

    protected open fun pause() {
        isPaused = true

        recordingTimeTimer.shutdown()
    }

    protected open fun resume() {
        createRecordingTimeTimer()
    }

    protected open suspend fun stop() {
        recordingTimeTimer.shutdown()
    }

    protected abstract fun startForegroundService()

    fun startRecording() {
        recordingStart = LocalDateTime.now()

        startForegroundService()
        changeState(RecorderState.RECORDING)

        try {
            start()
        } catch (error: RuntimeException) {
            error.printStackTrace()

            if (error !is AvoidErrorDialogError) {
                onError()
            }
        }
    }

    suspend fun stopRecording() {
        changeState(RecorderState.STOPPED)
        stop()
    }

    fun pauseRecording() {
        changeState(RecorderState.PAUSED)
    }

    fun resumeRecording() {
        changeState(RecorderState.RECORDING)
    }

    fun destroy() {
        NotificationManagerCompat.from(this)
            .cancel(NotificationHelper.RECORDER_CHANNEL_NOTIFICATION_ID)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "init" -> {
                notificationDetails = intent.getStringExtra("notificationDetails")?.let {
                    Json.decodeFromString(
                        RecorderNotificationHelper.NotificationDetails.serializer(),
                        it
                    )
                }
            }

            "changeState" -> {
                val newState = intent.getStringExtra("newState")?.let {
                    RecorderState.valueOf(it)
                } ?: RecorderState.STOPPED
                changeState(newState)
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    inner class RecorderBinder : Binder() {
        fun getService(): RecorderService = this@RecorderService
    }

    private fun createRecordingTimeTimer() {
        recordingTimeTimer = Executors.newSingleThreadScheduledExecutor().also {
            it.scheduleAtFixedRate(
                {
                    recordingTime += 1
                    onRecordingTimeChange?.invoke(recordingTime)
                },
                0,
                1,
                TimeUnit.SECONDS
            )
        }
    }

    // Used to change the state of the service
    // will internally call start() / pause() / resume() / stop()
    // Immediately after creating the service make sure to call `changeState(RecorderState.RECORDING)`
    @SuppressLint("MissingPermission")
    fun changeState(newState: RecorderState) {
        if (state == newState) {
            return
        }

        state = newState
        when (newState) {
            RecorderState.RECORDING -> {
                if (isPaused) {
                    resume()
                    isPaused = false
                }
                // `start` is handled by `startRecording`
            }

            RecorderState.PAUSED -> pause()

            else -> {}
        }

        // Update notification
        if (
            arrayOf(
                RecorderState.RECORDING,
                RecorderState.PAUSED
            ).contains(newState) &&
            PermissionHelper.hasGranted(this, android.Manifest.permission.POST_NOTIFICATIONS)
        ) {
            val notification = buildNotification()
            NotificationManagerCompat.from(this).notify(
                NotificationHelper.RECORDER_CHANNEL_NOTIFICATION_ID,
                notification
            )
        }

        onStateChange?.invoke(newState)
    }

    protected fun getNotificationHelper(): RecorderNotificationHelper {
        return RecorderNotificationHelper(this, notificationDetails)
    }

    private fun buildNotification(): Notification {
        val notificationHelper = getNotificationHelper()

        return when (state) {
            RecorderState.RECORDING -> {
                notificationHelper.buildRecordingNotification(recordingTime)
            }

            RecorderState.PAUSED -> {
                notificationHelper.buildPausedNotification(recordingStart)
            }

            else -> {
                throw IllegalStateException("Notification can't be built in state $state")
            }
        }
    }


    // Throw this error if you show a dialog yourself.
    // This will prevent the service from showing their generic error dialog.
    class AvoidErrorDialogError : RuntimeException()
}