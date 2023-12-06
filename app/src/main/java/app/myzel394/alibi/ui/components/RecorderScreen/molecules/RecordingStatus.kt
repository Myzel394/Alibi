package app.myzel394.alibi.ui.components.RecorderScreen.molecules

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.myzel394.alibi.ui.components.RecorderScreen.atoms.RecordingProgress
import app.myzel394.alibi.ui.components.RecorderScreen.atoms.RecordingTime

@Composable
fun RecordingStatus(
    recordingTime: Long,
    progress: Float,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        RecordingTime(recordingTime)
        Spacer(modifier = Modifier.height(16.dp))
        RecordingProgress(
            recordingTime = recordingTime,
            progress = progress,
        )
    }
}