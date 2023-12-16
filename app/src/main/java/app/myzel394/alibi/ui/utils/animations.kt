package app.myzel394.alibi.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

@Composable
fun rememberInitialRecordingAnimation(recordingTime: Long): Boolean {
    // Only show animation when the recording has just started
    val recordingJustStarted = recordingTime <= 1L
    var progressVisible by rememberSaveable { mutableStateOf(!recordingJustStarted) }

    LaunchedEffect(Unit) {
        progressVisible = true
    }

    return progressVisible
}
