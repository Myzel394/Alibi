package app.myzel394.locationtest.ui.components.AudioRecorder.atoms

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.myzel394.locationtest.services.RecorderService
import app.myzel394.locationtest.ui.components.atoms.Pulsating
import app.myzel394.locationtest.ui.utils.formatDuration
import app.myzel394.locationtest.ui.utils.rememberFileSaverDialog
import java.time.Duration
import java.time.LocalDateTime

@Composable
fun RecordingStatus(
    service: RecorderService,
) {
    val context = LocalContext.current

    val saveFile = rememberFileSaverDialog("audio/*")

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Forces real time update for the text
        val transition = rememberInfiniteTransition()
        val forceUpdateValue by transition.animateFloat(
            initialValue = .999999f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            )
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            val distance = Duration.between(service.recordingStart.value, LocalDateTime.now()).toMillis()

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
                modifier = Modifier.alpha(forceUpdateValue)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = service.progress,
            modifier = Modifier
                .width(300.dp)
                .alpha(forceUpdateValue)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
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
        Button(
            onClick = {
                RecorderService.stopService(context)
            },
            colors = ButtonDefaults.textButtonColors(),
        ) {
            Text("Cancel")
        }
    }
}