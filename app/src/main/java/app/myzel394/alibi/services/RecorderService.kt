package app.myzel394.alibi.services

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import app.myzel394.alibi.MainActivity
import app.myzel394.alibi.NotificationHelper
import app.myzel394.alibi.R
import app.myzel394.alibi.enums.RecorderState
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

abstract class RecorderService: Service() {
    private val binder = RecorderBinder()

    private var isPaused: Boolean = false

    lateinit var recordingStart: LocalDateTime
        private set

    var state = RecorderState.IDLE
        private set

    var onStateChange: ((RecorderState) -> Unit)? = null

    protected abstract fun start()
    protected abstract fun pause()
    protected abstract fun resume()
    protected abstract fun stop()

    override fun onBind(p0: Intent?): IBinder? = binder

    inner class RecorderBinder: Binder() {
        fun getService(): RecorderService = this@RecorderService
    }

    fun changeState(newState: RecorderState) {
        if (state == newState) {
            return
        }

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
            RecorderState.IDLE -> stop()
        }

        state = newState
        onStateChange?.invoke(newState)
    }

    // Must be called immediately after the service is created
    fun startRecording() {
        recordingStart = LocalDateTime.now()

        val notification = buildNotification()
        startForeground(NotificationHelper.RECORDER_CHANNEL_NOTIFICATION_ID, notification)

        // Start
        changeState(RecorderState.RECORDING)
    }

    override fun onCreate() {
        super.onCreate()

        startRecording()
    }

    override fun onDestroy() {
        super.onDestroy()

        changeState(RecorderState.IDLE)

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, "recorder")
            .setContentTitle("Recording Audio")
            .setContentText("Recording audio in background")
            .setSmallIcon(R.drawable.launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setUsesChronometer(true)
            .setChronometerCountDown(false)
            .setWhen(Date.from(recordingStart.atZone(ZoneId.systemDefault()).toInstant()).time)
            .setShowWhen(true)
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            )
            .build()
    }
}