package app.myzel394.alibi.ui.components.AudioRecorder.atoms

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.myzel394.alibi.R
import app.myzel394.alibi.services.RecorderService
import app.myzel394.alibi.ui.BIG_PRIMARY_BUTTON_SIZE
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun SaveRecordingButton(
    modifier: Modifier = Modifier,
    service: RecorderService,
    onSaveFile: (File) -> Unit,
    label: String = stringResource(R.string.ui_audioRecorder_action_save_label),
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isProcessingAudio by remember { mutableStateOf(false) }

    if (isProcessingAudio)
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
                    stringResource(R.string.ui_audioRecorder_action_save_processing_dialog_title),
                )
            },
             text = {
                 Column(
                     horizontalAlignment = Alignment.CenterHorizontally,
                 ) {
                     Text(
                         stringResource(R.string.ui_audioRecorder_action_save_processing_dialog_description),
                     )
                        Spacer(modifier = Modifier.height(32.dp))
                     LinearProgressIndicator()
                 }
            },
            confirmButton = {}
        )
    Button(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .height(BIG_PRIMARY_BUTTON_SIZE)
            .semantics {
                contentDescription = label
            }
            .then(modifier),
        onClick = {
            isProcessingAudio = true

            scope.launch {
                try {
                } catch (error: Exception) {
                    Log.getStackTraceString(error)
                } finally {
                    isProcessingAudio = false
                }
            }
        },
    ) {
        Icon(
            Icons.Default.Save,
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize),
        )
        Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
        Text(label)
    }
}