package app.myzel394.alibi.ui.components.RecorderScreen.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.myzel394.alibi.R
import app.myzel394.alibi.ui.components.RecorderScreen.atoms.RecordingProgress
import app.myzel394.alibi.ui.components.RecorderScreen.atoms.RecordingTime
import app.myzel394.alibi.ui.utils.isSameDay
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        RecordingTime(recordingTime)
        RecordingProgress(
            recordingTime = recordingTime,
            progress = progress,
        )

        Text(
            text = stringResource(
                R.string.ui_recorder_info_saveNowTime,
                DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT).format(
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
                        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
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