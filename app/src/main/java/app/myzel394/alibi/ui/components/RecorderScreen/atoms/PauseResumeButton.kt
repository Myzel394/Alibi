package app.myzel394.alibi.ui.components.RecorderScreen.atoms

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import app.myzel394.alibi.R

@Composable
fun PauseResumeButton(
    modifier: Modifier = Modifier,
    isPaused: Boolean,
    onChange: () -> Unit,
) {
    val pauseLabel = stringResource(R.string.ui_recorder_action_pause_label)
    val resumeLabel = stringResource(R.string.ui_recorder_action_resume_label)

    FloatingActionButton(
        modifier = Modifier
            .semantics {
                contentDescription = if (isPaused) resumeLabel else pauseLabel
            }
            .then(modifier),
        onClick = onChange,
    ) {
        Icon(
            if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
            contentDescription = null,
        )
    }
}