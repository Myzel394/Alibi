package app.myzel394.locationtest.ui.components.AudioRecorder.molecules

import android.Manifest
import android.content.ServiceConnection
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.myzel394.locationtest.R
import app.myzel394.locationtest.services.RecorderService
import app.myzel394.locationtest.ui.BIG_PRIMARY_BUTTON_SIZE
import app.myzel394.locationtest.ui.components.AudioRecorder.atoms.AudioVisualizer
import app.myzel394.locationtest.ui.components.atoms.PermissionRequester
import app.myzel394.locationtest.ui.utils.rememberFileSaverDialog
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun StartRecording(
    connection: ServiceConnection,
    service: RecorderService? = null,
) {
    val context = LocalContext.current

    val saveFile = rememberFileSaverDialog("audio/*")

    val hasAmplitudes = service?.amplitudes?.isNotEmpty() ?: false

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
        if (service?.recordingStart != null)
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
            ) {
                if (hasAmplitudes)
                    AudioVisualizer(
                        modifier = Modifier
                            .height(100.dp)
                            .padding(bottom = 32.dp),
                        amplitudes = service.amplitudes,
                    )
                Button(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .height(BIG_PRIMARY_BUTTON_SIZE),
                    onClick = {
                        saveFile(service.concatenateFiles())
                    }
                ) {
                    Icon(
                        Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier
                            .size(ButtonDefaults.IconSize),
                    )
                    Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                    Text(
                        stringResource(
                            R.string.ui_audioRecorder_action_saveOldRecording_label,
                            DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).format(service.recordingStart!!),
                        ),
                    )
                }
            }
        else
            Spacer(modifier = Modifier.weight(1f))
    }
}