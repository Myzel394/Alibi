package app.myzel394.alibi.ui.components.AudioRecorder.molecules

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.ui.components.AudioRecorder.atoms.MicrophoneDisconnectedDialog
import app.myzel394.alibi.ui.components.AudioRecorder.atoms.MicrophoneReconnectedDialog
import app.myzel394.alibi.ui.effects.rememberPrevious
import app.myzel394.alibi.ui.models.AudioRecorderModel
import app.myzel394.alibi.ui.utils.MicrophoneInfo

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
        if (microphoneStatus != previousStatus && showMicrophoneStatusDialog == null && previousStatus != null) {
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