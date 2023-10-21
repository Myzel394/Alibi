package app.myzel394.alibi.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import app.myzel394.alibi.MainActivity
import app.myzel394.alibi.NotificationHelper
import app.myzel394.alibi.R
import app.myzel394.alibi.enums.RecorderState
import app.myzel394.alibi.ui.utils.PermissionHelper
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
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

    protected abstract fun start()
    protected abstract fun pause()
    protected abstract fun resume()
    protected abstract fun stop()

    override fun onBind(p0: Intent?): IBinder? = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
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

            RecorderState.IDLE -> {
                stop()
                onDestroy()
            }
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

        val notification = buildStartNotification()
        startForeground(NotificationHelper.RECORDER_CHANNEL_NOTIFICATION_ID, notification)

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

    private fun buildStartNotification(): Notification =
        NotificationCompat.Builder(this, NotificationHelper.RECORDER_CHANNEL_ID)
            .setContentTitle(getString(R.string.ui_audioRecorder_state_recording_title))
            .setContentText(getString(R.string.ui_audioRecorder_state_recording_description))
            .setSmallIcon(R.drawable.launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

    private fun getNotificationChangeStateIntent(
        newState: RecorderState,
        requestCode: Int
    ): PendingIntent {
        return PendingIntent.getService(
            this,
            requestCode,
            Intent(this, AudioRecorderService::class.java).apply {
                action = "changeState"
                putExtra("newState", newState.name)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun buildNotification(): Notification = when (state) {
        RecorderState.RECORDING -> NotificationCompat.Builder(
            this,
            NotificationHelper.RECORDER_CHANNEL_ID
        )
            .setContentTitle(getString(R.string.ui_audioRecorder_state_recording_title))
            .setContentText(getString(R.string.ui_audioRecorder_state_recording_description))
            .setSmallIcon(R.drawable.launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(true)
            .setWhen(
                Date.from(
                    Calendar
                        .getInstance()
                        .also { it.add(Calendar.MILLISECOND, -recordingTime.toInt()) }
                        .toInstant()
                ).time,
            )
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .setUsesChronometer(true)
            .setChronometerCountDown(false)
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            )
            .addAction(
                R.drawable.ic_cancel,
                getString(R.string.ui_audioRecorder_action_delete_label),
                getNotificationChangeStateIntent(RecorderState.IDLE, 1),
            )
            .addAction(
                R.drawable.ic_pause,
                getString(R.string.ui_audioRecorder_action_pause_label),
                getNotificationChangeStateIntent(RecorderState.PAUSED, 2),
            )
            .build()

        RecorderState.PAUSED -> NotificationCompat.Builder(
            this,
            NotificationHelper.RECORDER_CHANNEL_ID
        )
            .setContentTitle(getString(R.string.ui_audioRecorder_state_paused_title))
            .setContentText(getString(R.string.ui_audioRecorder_state_paused_description))
            .setSmallIcon(R.drawable.launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(false)
            .setOnlyAlertOnce(true)
            .setUsesChronometer(false)
            .setWhen(Date.from(recordingStart.atZone(ZoneId.systemDefault()).toInstant()).time)
            .setShowWhen(true)
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE,
                )
            )
            .addAction(
                R.drawable.ic_play,
                getString(R.string.ui_audioRecorder_action_resume_label),
                getNotificationChangeStateIntent(RecorderState.RECORDING, 3),
            )
            .build()

        else -> throw IllegalStateException("Invalid state passed to `buildNotification()`")
    }
}