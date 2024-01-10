package app.myzel394.alibi.ui.components.RecorderScreen.atoms

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.myzel394.alibi.R
import app.myzel394.alibi.ui.utils.KeepScreenOn

@Composable
fun RecorderProcessingDialog(
    progress: Float?,
) {
    KeepScreenOn()
    AlertDialog(
        onDismissRequest = { },
        icon = {
            Icon(
                Icons.Default.Memory,
                contentDescription = null,
            )
        },
        title = {
            Text(
                stringResource(R.string.ui_recorder_action_save_processing_dialog_title),
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    stringResource(R.string.ui_recorder_action_save_processing_dialog_description),
                )
                Spacer(modifier = Modifier.height(32.dp))
                if (progress == null)
                    LinearProgressIndicator()
                else
                    LinearProgressIndicator(progress = progress)
            }
        },
        confirmButton = {}
    )
}