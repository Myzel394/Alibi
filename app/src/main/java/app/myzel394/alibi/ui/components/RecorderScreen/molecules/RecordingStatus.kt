package app.myzel394.alibi.ui.components.RecorderScreen.molecules

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.myzel394.alibi.R
import app.myzel394.alibi.ui.components.atoms.Pulsating
import app.myzel394.alibi.ui.utils.formatDuration
import app.myzel394.alibi.ui.utils.isSameDay
import app.myzel394.alibi.ui.utils.rememberInitialRecordingAnimation
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.math.min

@Composable
fun RecordingStatus(
    recordingTime: Long,
    progress: Float,
    recordingStart: LocalDateTime,
    maxDuration: Long,
) {
    val animateIn = rememberInitialRecordingAnimation(recordingTime)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
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
                text = formatDuration(recordingTime * 1000),
                style = MaterialTheme.typography.headlineLarge,
            )
        }

        AnimatedVisibility(
            visible = animateIn,
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

        AnimatedVisibility(visible = animateIn, enter = fadeIn()) {
            Text(
                text = stringResource(
                    R.string.ui_recorder_info_saveNowTime,
                    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
                        .format(
                            LocalDateTime.now().minusSeconds(
                                min(
                                    maxDuration / 1000,
                                    recordingTime
                                )
                            )
                        )
                ),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
        }

        AnimatedVisibility(visible = animateIn, enter = fadeIn()) {
            Text(
                text = recordingStart.let {
                    if (isSameDay(it, LocalDateTime.now())) {
                        stringResource(
                            R.string.ui_recorder_info_startTime_short,
                            DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
                                .format(it)
                        )
                    } else {
                        stringResource(
                            R.string.ui_recorder_info_startTime_full,
                            DateTimeFormatter.ofLocalizedDateTime(
                                FormatStyle.MEDIUM,
                                FormatStyle.SHORT
                            )
                                .format(it)
                        )
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}