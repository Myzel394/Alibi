package app.myzel394.alibi.ui.components.RecorderScreen.atoms

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import app.myzel394.alibi.R

@Composable
fun ConfirmDeletionDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {
            onDismiss()
        },
        title = {
            Text(stringResource(R.string.ui_audioRecorder_action_delete_confirm_title))
        },
        text = {
            Text(stringResource(R.string.ui_audioRecorder_action_delete_confirm_message))
        },
        icon = {
            Icon(
                Icons.Default.Delete,
                contentDescription = null,
            )
        },
        confirmButton = {
            val label = stringResource(R.string.ui_audioRecorder_action_delete_label)
            Button(
                modifier = Modifier
                    .semantics {
                        contentDescription = label
                    },
                onClick = {
                    onConfirm()
                },
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                )
                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                Text(label)
            }
        },
        dismissButton = {
            val label = stringResource(R.string.dialog_close_cancel_label)
            Button(
                modifier = Modifier
                    .semantics {
                        contentDescription = label
                    },
                onClick = {
                    onDismiss()
                },
                colors = ButtonDefaults.textButtonColors(),
            ) {
                Icon(
                    Icons.Default.Cancel,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                )
                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                Text(label)
            }
        }
    )
}