package app.myzel394.alibi.ui.models

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.documentfile.provider.DocumentFile
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.db.RecordingInformation
import app.myzel394.alibi.enums.RecorderState
import app.myzel394.alibi.helpers.AudioBatchesFolder
import app.myzel394.alibi.helpers.VideoBatchesFolder
import app.myzel394.alibi.services.AudioRecorderService
import app.myzel394.alibi.ui.RECORDER_MEDIA_SELECTED_VALUE
import app.myzel394.alibi.ui.utils.MicrophoneInfo

class AudioRecorderModel :
    BaseRecorderModel<RecordingInformation, AudioBatchesFolder, AudioRecorderService>() {
    override var batchesFolder: AudioBatchesFolder? = null
    override val intentClass = AudioRecorderService::class.java

    var amplitudes by mutableStateOf<List<Int>>(emptyList())
        private set
    var selectedMicrophone by mutableStateOf<MicrophoneInfo?>(null)
        private set

    var onAmplitudeChange: () -> Unit = {}

    var microphoneStatus: MicrophoneConnectivityStatus = MicrophoneConnectivityStatus.CONNECTED
        private set

    enum class MicrophoneConnectivityStatus {
        CONNECTED,
        DISCONNECTED
    }

    override fun onServiceConnected(service: AudioRecorderService) {
        service.onSelectedMicrophoneChange = { microphone ->
            selectedMicrophone = microphone
        }
        service.onMicrophoneDisconnected = {
            microphoneStatus = MicrophoneConnectivityStatus.DISCONNECTED
        }
        service.onMicrophoneReconnected = {
            microphoneStatus = MicrophoneConnectivityStatus.CONNECTED
        }
        service.onAmplitudeChange = { amps ->
            amplitudes = amps
            onAmplitudeChange()
        }

        // `onServiceConnected` may be called when reconnecting to the service,
        // so we only want to actually start the recording if the service is idle and thus
        // not already recording
        if (service.state == RecorderState.IDLE) {
            service.clearAllRecordings()
            service.startRecording()
            onRecordingStart()
        }

        recorderState = service.state
        recordingTime = service.recordingTime
        amplitudes = service.amplitudes
        selectedMicrophone = service.selectedMicrophone
    }

    override fun startRecording(context: Context, settings: AppSettings) {
        batchesFolder = when (settings.saveFolder) {
            null -> AudioBatchesFolder.viaInternalFolder(context)
            RECORDER_MEDIA_SELECTED_VALUE -> AudioBatchesFolder.viaMediaFolder(context)
            else -> AudioBatchesFolder.viaCustomFolder(
                context,
                DocumentFile.fromTreeUri(
                    context,
                    Uri.parse(settings.saveFolder)
                )!!
            )
        }

        super.startRecording(context, settings)
    }

    override fun reset() {
        super.reset()
        amplitudes = emptyList()
        selectedMicrophone = null
        microphoneStatus = MicrophoneConnectivityStatus.CONNECTED
    }

    fun setMaxAmplitudesAmount(amount: Int) {
        recorderService?.amplitudesAmount = amount
    }

    fun changeMicrophone(microphone: MicrophoneInfo?) {
        recorderService!!.changeMicrophone(microphone)

        if (microphone == null) {
            // Microphone was reset to default,
            // default is always assumed to be connected
            microphoneStatus = MicrophoneConnectivityStatus.CONNECTED
        }
    }
}