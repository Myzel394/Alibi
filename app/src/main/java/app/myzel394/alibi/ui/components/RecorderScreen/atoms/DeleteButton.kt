package app.myzel394.alibi.ui.components.RecorderScreen.atoms

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import app.myzel394.alibi.R

@Composable
fun DeleteButton(
    modifier: Modifier = Modifier,
    onDelete: () -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        ConfirmDeletionDialog(
            onDismiss = {
                showDeleteDialog = false
            },
            onConfirm = {
                showDeleteDialog = false
                onDelete()
            },
        )
    }
    val label = stringResource(R.string.ui_audioRecorder_action_delete_label)
    Button(
        modifier = Modifier
            .semantics {
                contentDescription = label
            }
            .then(modifier),
        onClick = {
            showDeleteDialog = true
        },
        colors = ButtonDefaults.textButtonColors(),
    ) {
        Text(
            label,
            fontSize = MaterialTheme.typography.bodySmall.fontSize,
        )
    }
}