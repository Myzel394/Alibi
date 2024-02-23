package app.myzel394.alibi.ui.components.RecorderScreen.molecules

import android.Manifest
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Mic
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import app.myzel394.alibi.R
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.ui.components.RecorderScreen.atoms.BigButton
import app.myzel394.alibi.ui.components.atoms.PermissionRequester
import app.myzel394.alibi.ui.models.AudioRecorderModel

@Composable
fun AudioRecordingStart(
    audioRecorder: AudioRecorderModel,
    appSettings: AppSettings,
) {
    val context = LocalContext.current

    // We can't get the current `notificationDetails` inside the
    // `onPermissionAvailable` function. We'll instead use this hack
    // with `LaunchedEffect` to get the current value.
    var startRecording by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(startRecording) {
        if (startRecording) {
            startRecording = false

            audioRecorder.startRecording(context, appSettings)
        }
    }

    PermissionRequester(
        permission = Manifest.permission.WRITE_EXTERNAL_STORAGE,
        icon = Icons.AutoMirrored.Filled.InsertDriveFile,
        onPermissionAvailable = {
            startRecording = true
        }
    ) { triggerExternalStorage ->
        PermissionRequester(
            permission = Manifest.permission.RECORD_AUDIO,
            icon = Icons.Default.Mic,
            onPermissionAvailable = {
                if (appSettings.requiresExternalStoragePermission(context)) {
                    triggerExternalStorage()
                } else {
                    startRecording = true
                }
            }
        ) { triggerRecordAudio ->
            BigButton(
                label = stringResource(R.string.ui_audioRecorder_action_start_label),
                icon = Icons.Default.Mic,
                onClick = triggerRecordAudio,
            )
        }
    }
}