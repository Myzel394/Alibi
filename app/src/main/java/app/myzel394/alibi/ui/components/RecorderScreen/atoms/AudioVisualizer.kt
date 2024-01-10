package app.myzel394.alibi.ui.components.RecorderScreen.atoms

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import app.myzel394.alibi.ui.MAX_AMPLITUDE

// Inspired by https://github.com/Bnyro/RecordYou/blob/main/app/src/main/java/com/bnyro/recorder/ui/components/AudioVisualizer.kt

@Composable
fun AudioVisualizer(
    modifier: Modifier = Modifier,
    amplitudes: List<Int>,
) {
    val primary = MaterialTheme.colorScheme.primary
    val primaryMuted = primary.copy(alpha = 0.3f)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
    ) {
        val height = this.size.height / 2f
        val width = this.size.width
        val boxWidth = width / amplitudes.size

        amplitudes.forEachIndexed { index, amplitude ->
            val x = boxWidth * index
            val amplitudePercentage = (amplitude.toFloat() / MAX_AMPLITUDE).coerceAtMost(1f)
            val boxHeight = height * amplitudePercentage

            drawRoundRect(
                color = if (amplitudePercentage > 0.05f) primary else primaryMuted,
                topLeft = Offset(x, -boxHeight / 2f),
                size = Size(boxWidth, boxHeight),
                cornerRadius = CornerRadius(3f, 3f)
            )
        }
    }
}