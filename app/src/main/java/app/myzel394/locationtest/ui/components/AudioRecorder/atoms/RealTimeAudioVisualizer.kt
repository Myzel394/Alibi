package app.myzel394.locationtest.ui.components.AudioRecorder.atoms

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import app.myzel394.locationtest.services.RecorderService
import kotlinx.coroutines.launch

private const val MAX_AMPLITUDE = 10000
private const val BOX_WIDTH = 15f
private const val BOX_GAP = 15f

@Composable
fun RealtimeAudioVisualizer(
    service: RecorderService,
) {
    val scope = rememberCoroutineScope()
    val amplitudes = service.amplitudes
    val primary = MaterialTheme.colorScheme.primary
    val primaryMuted = primary.copy(alpha = 0.3f)

    // Moves the visualizer to the left
    // A new amplitude is added every 100L milliseconds, so the visualizer moves one
    // box width + gap in 100L
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        service.setOnAmplitudeUpdateListener {
            scope.launch {
                animationProgress.snapTo(0f)
                animationProgress.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(
                        durationMillis = 100,
                        easing = LinearEasing
                    )
                )
            }
        }
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
    ) {
        val height = this.size.height / 2f
        val width = this.size.width

        translate(width, height) {
            translate(-animationProgress.value * (BOX_WIDTH + BOX_GAP), 0f) {
                amplitudes.forEachIndexed { index, amplitude ->
                    val amplitudePercentage = (amplitude.toFloat() / MAX_AMPLITUDE).coerceAtMost(1f)
                    val boxHeight = height * amplitudePercentage

                    drawRoundRect(
                        color = if (amplitudePercentage > 0.05f) primary else primaryMuted,
                        topLeft = Offset(
                            (BOX_WIDTH + BOX_GAP) * (index - amplitudes.size),
                            -boxHeight / 2f
                        ),
                        size = Size(BOX_WIDTH, boxHeight),
                        cornerRadius = CornerRadius(3f, 3f)
                    )
                }
            }
        }
    }
}
