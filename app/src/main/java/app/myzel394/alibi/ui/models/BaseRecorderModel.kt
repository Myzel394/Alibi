package app.myzel394.alibi.ui.models

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.IBinder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.enums.RecorderState
import app.myzel394.alibi.helpers.BatchesFolder
import app.myzel394.alibi.services.AudioRecorderService
import app.myzel394.alibi.services.IntervalRecorderService
import app.myzel394.alibi.services.RecorderNotificationHelper
import app.myzel394.alibi.services.RecorderService
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json

abstract class BaseRecorderModel<S : IntervalRecorderService.Settings, I, T : IntervalRecorderService<S, I>, B : BatchesFolder?> :
    ViewModel() {
    protected abstract val intentClass: Class<T>

    var recorderState by mutableStateOf(RecorderState.IDLE)
        protected set
    var recordingTime by mutableStateOf<Long?>(null)
        protected set

    val isInRecording: Boolean
        get() = recorderState !== RecorderState.IDLE && recordingTime != null

    val isPaused: Boolean
        get() = recorderState === RecorderState.PAUSED

    val progress: Float
        get() = (recordingTime!! / recorderService!!.settings.maxDuration).toFloat()

    var recorderService: T? = null
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
            recorderService = null
            reset()
        }
    }

    open fun reset() {
        recorderState = RecorderState.IDLE
        recordingTime = null
    }

    protected open fun handleIntent(intent: Intent) = intent

    // If override, call `super` AFTER setting the settings
    open fun startRecording(
        context: Context,
        settings: AppSettings,
    ) {
        this.settings = settings

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
        recorderService!!.stopRecording()

        val intent = Intent(context, intentClass)

        context.unbindService(connection)
        context.stopService(intent)
    }

    fun pauseRecording() {
        recorderService!!.changeState(RecorderState.PAUSED)
    }

    fun resumeRecording() {
        recorderService!!.changeState(RecorderState.RECORDING)
    }

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