package app.myzel394.locationtest.ui.components.AudioRecorder.atoms

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp

// Inspired by https://github.com/Bnyro/RecordYou/blob/main/app/src/main/java/com/bnyro/recorder/ui/components/AudioVisualizer.kt

private const val MAX_AMPLITUDE = 10000

@Composable
fun AudioVisualizer(
    amplitudes: List<Int>,
) {
    val primary = MaterialTheme.colorScheme.primary
    val primaryMuted = primary.copy(alpha = 0.3f)

    Canvas(
        modifier = Modifier.width(300.dp).height(300.dp)
    ) {
        val height = this.size.height / 2f
        val width = this.size.width

        translate(width, height) {
            amplitudes.forEachIndexed { index, amplitude ->
                val amplitudePercentage = (amplitude.toFloat() / MAX_AMPLITUDE).coerceAtMost(1f)
                val boxHeight = height * amplitudePercentage
                drawRoundRect(
                    color = if (amplitudePercentage > 0.05f) primary else primaryMuted,
                    topLeft = Offset(
                        30f * (index - amplitudes.size),
                        -boxHeight / 2f
                    ),
                    size = Size(15f, boxHeight),
                    cornerRadius = CornerRadius(3f, 3f)
                )
            }
        }
    }
}