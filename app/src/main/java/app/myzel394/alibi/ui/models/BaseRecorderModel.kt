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

abstract class BaseRecorderModel<I, B : BatchesFolder, T : IntervalRecorderService<I, B>> :
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
        get() = recordingTime.toFloat() / (recorderService!!.settings.maxDuration / 1000)

    var recorderService by mutableStateOf<T?>(null)
        protected set

    val recordingStart
        get() = recorderService!!.recordingStart

    // If `isSavingAsOldRecording` is true, the user is saving an old recording,
    // thus the service is not running and thus doesn't need to be stopped or destroyed
    var onRecordingSave: (isSavingAsOldRecording: Boolean) -> Unit = {}
    var onError: () -> Unit = {}
    var onBatchesFolderNotAccessible: () -> Unit = {}
    abstract var batchesFolder: B?

    private var notificationDetails: RecorderNotificationHelper.NotificationDetails? = null

    var settings: AppSettings? = null
        protected set

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
                    recorder.onBatchesFolderNotAccessible = {
                        onBatchesFolderNotAccessible()
                    }

                    if (batchesFolder != null) {
                        recorder.batchesFolder = batchesFolder!!
                    } else {
                        batchesFolder = recorder.batchesFolder
                    }

                    if (settings != null) {
                        // If `settings` is set, it means we started the recording, so it should be
                        // properly set on the service
                        recorder.settings = settings!!
                    } else {
                        settings = recorder.settings
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

    private fun stopOldServices(context: Context) {
        runCatching {
            context.unbindService(connection)
        }

        val intent = Intent(context, intentClass)
        runCatching {
            context.stopService(intent)
        }
    }

    // If override, call `super` AFTER setting the settings
    open fun startRecording(
        context: Context,
        settings: AppSettings,
    ) {
        this.settings = settings

        // Clean up
        stopOldServices(context)

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
    }

    fun pauseRecording() {
        recorderService!!.pauseRecording()
    }

    fun resumeRecording() {
        recorderService!!.resumeRecording()
    }

    fun destroyService(context: Context) {
        recorderService!!.destroy()

        stopOldServices(context)
        reset()
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