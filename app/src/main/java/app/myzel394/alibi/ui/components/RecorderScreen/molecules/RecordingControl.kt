package app.myzel394.alibi.ui.components.RecorderScreen.molecules

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import app.myzel394.alibi.ui.components.RecorderScreen.atoms.DeleteButton
import app.myzel394.alibi.ui.components.RecorderScreen.atoms.PauseResumeButton
import app.myzel394.alibi.ui.components.RecorderScreen.atoms.SaveButton
import app.myzel394.alibi.ui.utils.RandomStack
import app.myzel394.alibi.ui.utils.rememberInitialRecordingAnimation
import kotlinx.coroutines.delay

@Composable
fun RecordingControl(
    modifier: Modifier = Modifier,
    orientation: Int = LocalConfiguration.current.orientation,
    initialDelay: Long = 0L,
    isPaused: Boolean,
    recordingTime: Long,
    onDelete: () -> Unit,
    onPauseResume: () -> Unit,
    onSaveAndStop: () -> Unit,
    onSaveCurrent: () -> Unit,
) {
    val animateIn = rememberInitialRecordingAnimation(recordingTime)

    var deleteButtonAlphaIsIn by rememberSaveable {
        mutableStateOf(false)
    }
    val deleteButtonAlpha by animateFloatAsState(
        if (deleteButtonAlphaIsIn) 1f else 0f,
        label = "deleteButtonAlpha",
        animationSpec = tween(durationMillis = 500)
    )

    var pauseButtonAlphaIsIn by rememberSaveable {
        mutableStateOf(false)
    }
    val pauseButtonAlpha by animateFloatAsState(
        if (pauseButtonAlphaIsIn) 1f else 0f,
        label = "pauseButtonAlpha",
        animationSpec = tween(durationMillis = 500)
    )

    var saveButtonAlphaIsIn by rememberSaveable {
        mutableStateOf(false)
    }
    val saveButtonAlpha by animateFloatAsState(
        if (saveButtonAlphaIsIn) 1f else 0f,
        label = "saveButtonAlpha",
        animationSpec = tween(durationMillis = 500)
    )

    LaunchedEffect(animateIn) {
        if (animateIn) {
            delay(initialDelay)

            val stack = RandomStack.of(arrayOf(1, 2, 3).asIterable())

            while (!stack.isEmpty()) {
                when (stack.popRandom()) {
                    1 -> {
                        deleteButtonAlphaIsIn = true
                    }

                    2 -> {
                        pauseButtonAlphaIsIn = true
                    }

                    3 -> {
                        saveButtonAlphaIsIn = true
                    }
                }

                delay(250)
            }
        }
    }

    when (orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = modifier,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(saveButtonAlpha),
                    contentAlignment = Alignment.Center,
                ) {
                    SaveButton(
                        onSave = onSaveAndStop,
                        onLongClick = onSaveCurrent,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(pauseButtonAlpha),
                    contentAlignment = Alignment.Center,
                ) {
                    PauseResumeButton(
                        isPaused = isPaused,
                        onChange = onPauseResume,
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(deleteButtonAlpha),
                    contentAlignment = Alignment.Center,
                ) {
                    DeleteButton(
                        onDelete = onDelete,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        else -> {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .alpha(deleteButtonAlpha),
                    contentAlignment = Alignment.Center,
                ) {
                    DeleteButton(onDelete = onDelete)
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .alpha(pauseButtonAlpha),
                ) {
                    PauseResumeButton(
                        isPaused = isPaused,
                        onChange = onPauseResume,
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .alpha(saveButtonAlpha),
                    contentAlignment = Alignment.Center,
                ) {
                    SaveButton(
                        onSave = onSaveAndStop,
                        onLongClick = onSaveCurrent,
                    )
                }
            }
        }
    }
}