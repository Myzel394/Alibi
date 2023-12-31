package app.myzel394.alibi.ui.components.RecorderScreen.atoms

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
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
fun BatchesInaccessibleDialog(
    onClose: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onClose,
        icon = {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
            )
        },
        title = {
            Text(stringResource(R.string.ui_audioRecorder_error_recording_title))
        },
        text = {
            Text(stringResource(R.string.ui_audioRecorder_error_batchesInaccessible_description))
        },
        confirmButton = {
            Button(
                onClick = onClose,
                colors = ButtonDefaults.textButtonColors(),
            ) {
                Text(stringResource(R.string.dialog_close_neutral_label))
            }
        }
    )
}