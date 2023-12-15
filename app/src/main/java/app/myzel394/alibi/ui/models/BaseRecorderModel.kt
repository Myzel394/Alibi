package app.myzel394.alibi.ui.models

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.enums.RecorderState
import app.myzel394.alibi.helpers.BatchesFolder
import app.myzel394.alibi.services.IntervalRecorderService
import app.myzel394.alibi.services.RecorderNotificationHelper
import app.myzel394.alibi.services.RecorderService
import kotlinx.serialization.json.Json

abstract class BaseRecorderModel<S : IntervalRecorderService.Settings, I, T : IntervalRecorderService<S, I>, B : BatchesFolder?> :
    ViewModel() {
    protected abstract val intentClass: Class<T>

    var recorderState by mutableStateOf(RecorderState.IDLE)
        protected set
    var recordingTime by mutableLongStateOf(0)
        protected set

    open val isInRecording: Boolean
        get() = recorderService != null

    val isPaused: Boolean
        get() = recorderState === RecorderState.PAUSED

    val progress: Float
        get() = (recordingTime / recorderService!!.settings.maxDuration).toFloat()

    var recorderService by mutableStateOf<T?>(null)
        protected set

    var onRecordingSave: () -> Unit = {}
    var onError: () -> Unit = {}
    abstract var batchesFolder: B

    private var notificationDetails: RecorderNotificationHelper.NotificationDetails? = null

    protected lateinit var settings: AppSettings

    protected abstract fun onServiceConnected(service: T)

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            recorderService =
                ((service as RecorderService.RecorderBinder).getService() as T).also { recorder ->
                    // Init variables from us to the service
                    recorder.onStateChange = { state ->
                        recorderState = state
                    }
                    recorder.onRecordingTimeChange = { time ->
                        recordingTime = time
                    }
                    recorder.onError = {
                        onError()
                    }

                    if (batchesFolder != null) {
                        recorder.batchesFolder = batchesFolder!!
                    } else {
                        batchesFolder = recorder.batchesFolder as B
                    }

                    // Rest should be initialized from the child class
                    onServiceConnected(recorder)
                }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            // `onServiceDisconnected` is called when the connection is unexpectedly lost,
            // so we need to make sure to manually call `reset` to clean up in other places
            reset()
        }
    }

    open fun reset() {
        recorderService = null
        recorderState = RecorderState.IDLE
        recordingTime = 0
    }

    protected open fun handleIntent(intent: Intent) = intent

    // If override, call `super` AFTER setting the settings
    open fun startRecording(
        context: Context,
        settings: AppSettings,
    ) {
        this.settings = settings

        // Clean up
        runCatching {
            recorderService?.clearAllRecordings()
            context.unbindService(connection)
        }

        notificationDetails = settings.notificationSettings.let {
            if (it == null)
                null
            else
                RecorderNotificationHelper.NotificationDetails.fromNotificationSettings(
                    context,
                    it
                )
        }

        val intent = Intent(context, intentClass).apply {
            action = "init"

            if (notificationDetails != null) {
                putExtra(
                    "notificationDetails",
                    Json.encodeToString(
                        RecorderNotificationHelper.NotificationDetails.serializer(),
                        notificationDetails!!,
                    ),
                )
            }
        }.let(::handleIntent)
        ContextCompat.startForegroundService(context, intent)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    suspend fun stopRecording(context: Context) {
        // TODO: Make modal on video only appear on long press and by default use back camera
        // TODO: Also show what camera is in use while recording
        recorderService!!.stopRecording()

        runCatching {
            context.unbindService(connection)
        }
    }

    fun pauseRecording() {
        recorderService!!.pauseRecording()
    }

    fun resumeRecording() {
        recorderService!!.resumeRecording()
    }

    fun destroyService(context: Context) {
        recorderService!!.destroy()
        reset()
        val intent = Intent(context, intentClass)

        runCatching {
            context.stopService(intent)
        }
    }

    // Bind functions used to manually bind to the service if the app
    // is closed and reopened for example
    fun bindToService(context: Context) {
        Intent(context, intentClass).also { intent ->
            context.bindService(intent, connection, 0)
        }
    }

    fun unbindFromService(context: Context) {
        runCatching {
            context.unbindService(connection)
        }
    }
}