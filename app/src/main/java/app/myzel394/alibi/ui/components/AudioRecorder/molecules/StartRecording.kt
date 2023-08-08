package app.myzel394.alibi.ui.components.AudioRecorder.molecules

import android.Manifest
import android.content.ServiceConnection
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.myzel394.alibi.R
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.services.RecorderService
import app.myzel394.alibi.ui.BIG_PRIMARY_BUTTON_SIZE
import app.myzel394.alibi.ui.components.AudioRecorder.atoms.AudioVisualizer
import app.myzel394.alibi.ui.components.AudioRecorder.atoms.SaveRecordingButton
import app.myzel394.alibi.ui.components.atoms.PermissionRequester
import app.myzel394.alibi.ui.utils.rememberFileSaverDialog
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun StartRecording(
    connection: ServiceConnection,
    service: RecorderService? = null,
    onStart: () -> Unit,
) {
    val context = LocalContext.current
    val saveFile = rememberFileSaverDialog("audio/*")

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))
        PermissionRequester(
            permission = arrayOf(Manifest.permission.RECORD_AUDIO),
            icon = {
                Icon(
                    Icons.Default.Mic,
                    contentDescription = null,
                )
            },
            onPermissionAvailable = {
                RecorderService.startService(context, connection)

                if (service == null) {
                    onStart()
                } else {
                    // To avoid any leaks from the previous recording, we need to wait until it
                    // fully started
                    service.setOnStartedListener {
                        onStart()
                    }
                }
            },
        ) { trigger ->
            val label = stringResource(R.string.ui_audioRecorder_action_start_label)
            Button(
                onClick = {
                    trigger()
                },
                modifier = Modifier
                    .semantics {
                        contentDescription = label
                    }
                    .size(200.dp)
                    .clip(shape = CircleShape),
                colors = ButtonDefaults.outlinedButtonColors(),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp),
                    )
                    Spacer(modifier = Modifier.height(ButtonDefaults.IconSpacing))
                    Text(
                        label,
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
            }
        }
        val settings = LocalContext
            .current
            .dataStore
            .data
            .collectAsState(initial = AppSettings.getDefaultInstance())
            .value

        Text(
            stringResource(R.string.ui_audioRecorder_action_start_description, settings.audioRecorderSettings.maxDuration / 1000 / 60),
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            modifier = Modifier
                .widthIn(max = 300.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        if (service?.hasRecordingAvailable == true)
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
            ) {
                SaveRecordingButton(
                    service = service,
                    onSaveFile = saveFile,
                    label = stringResource(
                        R.string.ui_audioRecorder_action_saveOldRecording_label,
                        DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).format(service.recordingStart),
                    ),

                )
            }
        else
            Spacer(modifier = Modifier.weight(1f))
    }
}