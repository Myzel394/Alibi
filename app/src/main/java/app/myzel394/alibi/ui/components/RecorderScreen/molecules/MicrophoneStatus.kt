package app.myzel394.alibi.ui.components.RecorderScreen.molecules

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.myzel394.alibi.ui.components.RecorderScreen.atoms.MicrophoneDisconnectedDialog
import app.myzel394.alibi.ui.components.RecorderScreen.atoms.MicrophoneReconnectedDialog
import app.myzel394.alibi.ui.effects.rememberPrevious
import app.myzel394.alibi.ui.models.AudioRecorderModel

@Composable
fun MicrophoneStatus(
    audioRecorder: AudioRecorderModel,
) {
    val microphoneStatus = audioRecorder.microphoneStatus
    val previousStatus = rememberPrevious(microphoneStatus)

    var showMicrophoneStatusDialog by remember {
        // null = no dialog
        // `MicrophoneConnectivityStatus.CONNECTED` = Reconnected dialog
        // `MicrophoneConnectivityStatus.DISCONNECTED` = Disconnected dialog
        mutableStateOf<AudioRecorderModel.MicrophoneConnectivityStatus?>(null)
    }

    LaunchedEffect(microphoneStatus) {
        if (microphoneStatus != previousStatus && showMicrophoneStatusDialog == null && previousStatus != null && audioRecorder.selectedMicrophone != null) {
            showMicrophoneStatusDialog = microphoneStatus
        }
    }

    if (showMicrophoneStatusDialog == AudioRecorderModel.MicrophoneConnectivityStatus.DISCONNECTED) {
        MicrophoneDisconnectedDialog(
            onClose = {
                showMicrophoneStatusDialog = null
            },
            microphoneName = audioRecorder.selectedMicrophone?.name ?: "",
        )
    }

    if (showMicrophoneStatusDialog == AudioRecorderModel.MicrophoneConnectivityStatus.CONNECTED) {
        MicrophoneReconnectedDialog(
            onClose = {
                showMicrophoneStatusDialog = null
            },
            microphoneName = audioRecorder.selectedMicrophone?.name ?: "",
        )
    }

    MicrophoneSelection(
        audioRecorder = audioRecorder,
    )
}