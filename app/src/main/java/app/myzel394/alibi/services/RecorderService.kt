package app.myzel394.alibi.services

import android.annotation.SuppressLint
import app.myzel394.alibi.recording.RecordingRuntime
import app.myzel394.alibi.recording.RecordingIndicator
import app.myzel394.alibi.recording.RecordingHaptics
import app.myzel394.alibi.dataStore
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.flow.collect
import android.widget.Toast
import app.myzel394.alibi.R
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
    private val indicator by lazy { RecordingIndicator(this) }
    private var stopping = false
    private val stopMutex = kotlinx.coroutines.sync.Mutex()
    private var didStop = false
    protected var shortcutRequested = false
    private var shortcutConfirmed = false

    companion object {
        const val ACTION_START_VIDEO = "app.myzel394.alibi.START_VIDEO"
        const val ACTION_STOP = "app.myzel394.alibi.STOP_RECORDING"
    }

    private var indicatorEnabled = false
    protected open fun showRecordingIndicator(): Boolean = indicatorEnabled
    override fun onCreate() {
        super.onCreate()
        lifecycleScope.launch {
            dataStore.data.collect { settings ->
                indicatorEnabled = settings.recordingIndicatorEnabled
                if (state == RecorderState.RECORDING && indicatorEnabled) indicator.show() else indicator.hide()
            }
        }
    }

    protected fun confirmShortcutStart() {
        if (shortcutRequested && !shortcutConfirmed) {
            shortcutConfirmed = true
            RecordingHaptics.signal(this, true)
        }
    }

    protected fun reportStartFailure() {
        if (shortcutRequested) RecordingHaptics.signal(this)
        Toast.makeText(this, R.string.shortcut_start_failed, Toast.LENGTH_LONG).show()
        requestStopFromShortcut(false)
    }

    fun requestStopFromShortcut(vibrate: Boolean = true) {
        if (stopping) return
        stopping = true
        lifecycleScope.launch {
            try {
                stopRecording()
                val recording = (this@RecorderService as? IntervalRecorderService<*, *>)?.getRecordingInformation()
                if (recording is app.myzel394.alibi.db.RecordingInformation)
                    dataStore.updateData { it.setLastRecording(recording) }
                if (vibrate) RecordingHaptics.signal(this@RecorderService, false)
            } catch (error: Exception) {
                error.printStackTrace()
                RecordingHaptics.signal(this@RecorderService)
            } finally { destroy() }
        }
    }

    override fun onDestroy() {
        indicator.hide()
        RecordingRuntime.remove(this)
        if (::recordingTimeTimer.isInitialized) recordingTimeTimer.shutdownNow()
        onStateChange?.invoke(RecorderState.IDLE)
        super.onDestroy()
    }

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
        if (::recordingTimeTimer.isInitialized) recordingTimeTimer.shutdown()
    }

    protected abstract fun startForegroundService()

    fun startRecording() {
        recordingStart = LocalDateTime.now()

        try {
            startForegroundService()
            changeState(RecorderState.RECORDING)
            start()
        } catch (error: RuntimeException) {
            error.printStackTrace()

            reportStartFailure()
        }
    }

    suspend fun stopRecording() = stopMutex.withLock {
        if (!didStop) {
            changeState(RecorderState.STOPPED)
            stop()
            didStop = true
        }
    }

    fun pauseRecording() {
        changeState(RecorderState.PAUSED)
    }

    fun resumeRecording() {
        changeState(RecorderState.RECORDING)
    }

    fun destroy() {
        indicator.hide()
        RecordingRuntime.remove(this)
        onStateChange?.invoke(RecorderState.IDLE)
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
        if (intent?.action == ACTION_STOP) {
            requestStopFromShortcut()
            return android.app.Service.START_NOT_STICKY
        }
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

        super.onStartCommand(intent, flags, startId)
        return android.app.Service.START_NOT_STICKY
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
        RecordingRuntime.update(this, newState)
        if (newState == RecorderState.RECORDING && showRecordingIndicator()) indicator.show() else indicator.hide()
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