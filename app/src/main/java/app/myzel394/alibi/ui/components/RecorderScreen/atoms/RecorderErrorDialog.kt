package app.myzel394.alibi.ui.components.RecorderScreen.atoms

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.myzel394.alibi.R

@Composable
fun RecorderErrorDialog(
    onClose: () -> Unit,
    onSave: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onClose,
        icon = {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
            )
        },
        title = {
            Text(stringResource(R.string.ui_audioRecorder_error_recording_title))
        },
        text = {
            Text(stringResource(R.string.ui_audioRecorder_error_recording_description))
        },
        dismissButton = {
            Button(
                onClick = onClose,
                colors = ButtonDefaults.textButtonColors(),
            ) {
                Text(stringResource(R.string.dialog_close_cancel_label))
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                colors = ButtonDefaults.textButtonColors(),
            ) {
                Text(stringResource(R.string.ui_audioRecorder_action_save_label))
            }
        }
    )
}