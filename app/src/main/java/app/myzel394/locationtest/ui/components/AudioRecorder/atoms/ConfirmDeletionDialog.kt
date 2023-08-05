package app.myzel394.locationtest.ui.components.AudioRecorder.atoms

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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import app.myzel394.locationtest.services.RecorderService

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
            Text("Delete Recording?")
        },
        text = {
            Text("Are you sure you want to delete this recording?")
        },
        icon = {
            Icon(
                Icons.Default.Delete,
                contentDescription = null,
            )
        },
        confirmButton = {
            Button(
                modifier = Modifier
                    .semantics {
                        contentDescription = "Confirm Recording Deletion"
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
                Text("Delete")
            }
        },
        dismissButton = {
            Button(
                modifier = Modifier
                    .semantics {
                        contentDescription = "Cancel Recording Deletion"
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
                Text("Cancel")
            }
        }
    )
}