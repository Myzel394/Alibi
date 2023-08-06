package app.myzel394.alibi.ui.components.AudioRecorder.atoms

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import app.myzel394.alibi.services.RecorderService
import app.myzel394.alibi.ui.MAX_AMPLITUDE
import app.myzel394.alibi.ui.utils.clamp
import kotlinx.coroutines.launch
import kotlin.math.ceil

private const val BOX_WIDTH = 15f
private const val BOX_GAP = 15f
private const val BOX_DIFF = BOX_WIDTH + BOX_GAP
private const val GROW_START_INDEX = 2
private const val GROW_START = BOX_DIFF * GROW_START_INDEX
private const val GROW_END = BOX_DIFF * 4

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

    val configuration = LocalConfiguration.current
    val screenWidth = with (LocalDensity.current) {configuration.screenWidthDp.dp.toPx()}

    LaunchedEffect(screenWidth) {
        service.maxAmplitudes =  ceil(screenWidth.toInt() / BOX_DIFF).toInt()
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
                    val offset = amplitudes.size - index
                    val horizontalValue = offset * BOX_DIFF + animationProgress.value * BOX_DIFF
                    val isOverThreshold = offset >= GROW_START_INDEX
                    val horizontalProgress = (
                            clamp(horizontalValue, GROW_START, GROW_END)
                    - GROW_START) / (GROW_END - GROW_START)
                    val amplitudePercentage = (amplitude.toFloat() / MAX_AMPLITUDE).coerceAtMost(1f)
                    val boxHeight = (height * amplitudePercentage * horizontalProgress).coerceAtLeast(15f)

                    drawRoundRect(
                        color = if (amplitudePercentage > 0.05f && isOverThreshold) primary else primaryMuted,
                        topLeft = Offset(
                            BOX_DIFF * (index - amplitudes.size),
                            -boxHeight / 2f
                        ),
                        size = Size(BOX_WIDTH, boxHeight),
                        cornerRadius = CornerRadius(3f, 3f),
                    )
                }
            }
        }
    }
}
