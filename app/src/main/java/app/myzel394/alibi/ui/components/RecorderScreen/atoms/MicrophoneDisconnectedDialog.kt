package app.myzel394.alibi.ui.components.RecorderScreen.atoms

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import app.myzel394.alibi.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MicrophoneDisconnectedDialog(
    microphoneName: String,
    onClose: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onClose,
        title = {
            Text(
                stringResource(
                    R.string.ui_audioRecorder_error_microphoneDisconnected_title,
                ),
                textAlign = TextAlign.Center,
            )
        },
        text = {
            Text(
                stringResource(
                    R.string.ui_audioRecorder_error_microphoneDisconnected_message,
                    microphoneName,
                    microphoneName,
                )
            )
        },
        icon = {
            Icon(
                Icons.Default.MicOff,
                contentDescription = null,
            )
        },
        confirmButton = {
            val label = stringResource(R.string.dialog_close_neutral_label)

            Button(
                modifier = Modifier
                    .semantics {
                        contentDescription = label
                    },
                onClick = onClose,
            ) {
                Text(label)
            }
        }
    )
}