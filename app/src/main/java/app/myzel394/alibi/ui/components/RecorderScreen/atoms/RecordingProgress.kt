package app.myzel394.alibi.ui.components.RecorderScreen.atoms

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun RecordingProgress(
    recordingTime: Long,
    progress: Float,
) {
    // Only show animation when the recording has just started
    val recordingJustStarted = recordingTime <= 1000L
    var progressVisible by remember { mutableStateOf(!recordingJustStarted) }

    LaunchedEffect(Unit) {
        progressVisible = true
    }

    AnimatedVisibility(
        visible = progressVisible,
        enter = expandHorizontally(
            tween(1000)
        )
    ) {
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .width(300.dp)
        )
    }
}