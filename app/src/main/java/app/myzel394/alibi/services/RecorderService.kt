package app.myzel394.alibi.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import app.myzel394.alibi.NotificationHelper
import app.myzel394.alibi.enums.RecorderState
import app.myzel394.alibi.ui.utils.PermissionHelper
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


abstract class RecorderService : Service() {
    private val binder = RecorderBinder()

    private var isPaused: Boolean = false

    lateinit var recordingStart: LocalDateTime
        private set

    var state = RecorderState.IDLE
        private set

    var onStateChange: ((RecorderState) -> Unit)? = null

    var recordingTime = 0L
        private set
    private lateinit var recordingTimeTimer: ScheduledExecutorService
    var onRecordingTimeChange: ((Long) -> Unit)? = null
    var notificationDetails: RecorderNotificationHelper.NotificationDetails? = null

    protected abstract fun start()
    protected abstract fun pause()
    protected abstract fun resume()
    protected abstract fun stop()

    override fun onBind(p0: Intent?): IBinder? = binder

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
                } ?: RecorderState.IDLE
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
                    recordingTime += 1000
                    onRecordingTimeChange?.invoke(recordingTime)
                },
                0,
                1000,
                TimeUnit.MILLISECONDS
            )
        }
    }

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
                } else {
                    start()
                }
            }

            RecorderState.PAUSED -> {
                pause()
                isPaused = true
            }

            else -> {}
        }

        when (newState) {
            RecorderState.RECORDING -> {
                createRecordingTimeTimer()
            }

            RecorderState.PAUSED, RecorderState.IDLE -> {
                recordingTimeTimer.shutdown()
            }
        }


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

    // Must be immediately called after creating the service!
    fun startRecording() {
        recordingStart = LocalDateTime.now()

        ServiceCompat.startForeground(
            this,
            NotificationHelper.RECORDER_CHANNEL_NOTIFICATION_ID,
            getNotificationHelper().buildStartingNotification(),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            } else {
                0
            },
        )

        // Start
        changeState(RecorderState.RECORDING)
    }

    override fun onDestroy() {
        super.onDestroy()

        stop()
        changeState(RecorderState.IDLE)

        stopForeground(STOP_FOREGROUND_REMOVE)
        NotificationManagerCompat.from(this)
            .cancel(NotificationHelper.RECORDER_CHANNEL_NOTIFICATION_ID)
        stopSelf()
    }

    private fun getNotificationHelper(): RecorderNotificationHelper {
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
}