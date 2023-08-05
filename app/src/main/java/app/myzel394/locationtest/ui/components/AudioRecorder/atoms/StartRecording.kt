package app.myzel394.locationtest.ui.components.AudioRecorder.atoms

import android.content.ServiceConnection
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.myzel394.locationtest.services.RecorderService
import app.myzel394.locationtest.ui.BIG_PRIMARY_BUTTON_SIZE
import app.myzel394.locationtest.ui.utils.rememberFileSaverDialog
import java.time.format.DateTimeFormatter

@Composable
fun StartRecording(
    connection: ServiceConnection,
    service: RecorderService? = null,
) {
    val context = LocalContext.current

    val saveFile = rememberFileSaverDialog("audio/*")

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box {}
        if (service != null && service.amplitudes.isNotEmpty()) {
            Box {}
        }

        val primary = MaterialTheme.colorScheme.primary
        Button(
            onClick = {
                RecorderService.startService(context, connection)
            },
            modifier = Modifier
                .semantics {
                    contentDescription = "Start recording"
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
                    "Start Recording",
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        }
        if (service != null && service.amplitudes.isNotEmpty()) {
            AudioVisualizer(amplitudes = service.amplitudes)
        }
        if (service?.originalRecordingStart != null)
            Button(
                modifier = Modifier
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
                Text("Save Recording from ${service.originalRecordingStart!!.format(DateTimeFormatter.ISO_DATE_TIME)}")
            }
        else
            Box {}
    }
}