package app.myzel394.locationtest.ui.components.AudioRecorder.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.myzel394.locationtest.services.RecorderService
import app.myzel394.locationtest.ui.BIG_PRIMARY_BUTTON_SIZE
import app.myzel394.locationtest.ui.components.atoms.Pulsating
import app.myzel394.locationtest.ui.utils.formatDuration
import app.myzel394.locationtest.ui.utils.rememberFileSaverDialog
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId

@Composable
fun RecordingStatus(
    service: RecorderService,
) {
    val context = LocalContext.current
    val saveFile = rememberFileSaverDialog("audio/*")

    var now by remember { mutableStateOf(LocalDateTime.now()) }

    val start = service.recordingStart.value!!
    val duration = now.toEpochSecond(ZoneId.systemDefault().rules.getOffset(now)) - start.toEpochSecond(ZoneId.systemDefault().rules.getOffset(start))
    val progress = duration / (service.settings.maxDuration / 1000f)

    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(1000)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Box {}
        AudioVisualizer(amplitudes = service.amplitudes)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                val distance = Duration.between(service.recordingStart.value, now).toMillis()

                Pulsating {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = formatDuration(distance),
                    style = MaterialTheme.typography.headlineLarge,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .width(300.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    RecorderService.stopService(context)
                },
                colors = ButtonDefaults.textButtonColors(),
            ) {
                Text("Cancel")
            }
        }
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(BIG_PRIMARY_BUTTON_SIZE),
            onClick = {
                RecorderService.stopService(context)

                saveFile(service.concatenateFiles())
            },
        ) {
            Icon(
                Icons.Default.Save,
                contentDescription = null,
            )
            Text("Save Recording")
        }
    }
}