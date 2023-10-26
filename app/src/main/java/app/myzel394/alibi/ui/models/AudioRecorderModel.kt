package app.myzel394.alibi.ui.models

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import app.myzel394.alibi.db.RecordingInformation
import app.myzel394.alibi.enums.RecorderState
import app.myzel394.alibi.services.AudioRecorderService
import app.myzel394.alibi.services.RecorderNotificationHelper
import app.myzel394.alibi.services.RecorderService
import kotlinx.serialization.json.Json
import app.myzel394.alibi.ui.utils.MicrophoneInfo

class AudioRecorderModel : ViewModel() {
    var recorderState by mutableStateOf(RecorderState.IDLE)
        private set
    var recordingTime by mutableStateOf<Long?>(null)
        private set
    var amplitudes by mutableStateOf<List<Int>>(emptyList())
        private set
    var selectedMicrophone by mutableStateOf<MicrophoneInfo?>(null)
        private set

    var onAmplitudeChange: () -> Unit = {}

    val isInRecording: Boolean
        get() = recorderState !== RecorderState.IDLE && recordingTime != null

    val isPaused: Boolean
        get() = recorderState === RecorderState.PAUSED

    val progress: Float
        get() = (recordingTime!! / recorderService!!.settings!!.maxDuration).toFloat()

    var recorderService: AudioRecorderService? = null
        private set

    var lastRecording: RecordingInformation? by mutableStateOf<RecordingInformation?>(null)
        private set

    var onRecordingSave: () -> Unit = {}
    var onError: () -> Unit = {}
    var notificationDetails: RecorderNotificationHelper.NotificationDetails? = null

    var microphoneStatus: MicrophoneConnectivityStatus = MicrophoneConnectivityStatus.CONNECTED
        private set

    enum class MicrophoneConnectivityStatus {
        CONNECTED,
        DISCONNECTED
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            recorderService =
                ((service as RecorderService.RecorderBinder).getService() as AudioRecorderService).also { recorder ->
                    // Update UI when the service changes
                    recorder.onStateChange = { state ->
                        recorderState = state
                    }
                    recorder.onRecordingTimeChange = { time ->
                        recordingTime = time
                    }
                    recorder.onAmplitudeChange = { amps ->
                        amplitudes = amps
                        onAmplitudeChange()
                    }
                    recorder.onError = {
                        recorderService!!.createLastRecording()
                        onError()
                    }
                    recorder.onSelectedMicrophoneChange = { microphone ->
                        selectedMicrophone = microphone
                    }
                    recorder.onMicrophoneDisconnected = {
                        microphoneStatus = MicrophoneConnectivityStatus.DISCONNECTED
                    }
                    recorder.onMicrophoneReconnected = {
                        microphoneStatus = MicrophoneConnectivityStatus.CONNECTED
                    }
                }.also {
                    // Init UI from the service
                    it.startRecording()

                    recorderState = it.state
                    recordingTime = it.recordingTime
                    amplitudes = it.amplitudes
                    selectedMicrophone = it.selectedMicrophone
                }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            recorderService = null
            reset()
        }
    }

    fun reset() {
        recorderState = RecorderState.IDLE
        recordingTime = null
        amplitudes = emptyList()
        selectedMicrophone = null
        microphoneStatus = MicrophoneConnectivityStatus.CONNECTED
    }

    fun startRecording(context: Context) {
        runCatching {
            context.unbindService(connection)
        }

        val intent = Intent(context, AudioRecorderService::class.java).apply {
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
        }
        ContextCompat.startForegroundService(context, intent)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    fun stopRecording(context: Context, saveAsLastRecording: Boolean = true) {
        if (saveAsLastRecording) {
            lastRecording = recorderService!!.createLastRecording()
        }

        runCatching {
            context.unbindService(connection)
        }

        val intent = Intent(context, AudioRecorderService::class.java)
        context.stopService(intent)

        reset()
    }

    fun pauseRecording() {
        recorderService!!.changeState(RecorderState.PAUSED)
    }

    fun resumeRecording() {
        recorderService!!.changeState(RecorderState.RECORDING)
    }

    fun setMaxAmplitudesAmount(amount: Int) {
        recorderService?.amplitudesAmount = amount
    }

    fun changeMicrophone(microphone: MicrophoneInfo?) {
        recorderService!!.changeMicrophone(microphone)

        if (microphone == null) {
            microphoneStatus = MicrophoneConnectivityStatus.CONNECTED
        }
    }

    fun bindToService(context: Context) {
        Intent(context, AudioRecorderService::class.java).also { intent ->
            context.bindService(intent, connection, 0)
        }
    }

    fun unbindFromService(context: Context) {
        runCatching {
            context.unbindService(connection)
        }
    }
}