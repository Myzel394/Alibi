package app.myzel394.alibi.ui.components.RecorderScreen.organisms

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.myzel394.alibi.ui.components.RecorderScreen.atoms.DeleteButton
import app.myzel394.alibi.ui.components.RecorderScreen.atoms.PauseResumeButton
import app.myzel394.alibi.ui.components.RecorderScreen.atoms.RealtimeAudioVisualizer
import app.myzel394.alibi.ui.components.RecorderScreen.atoms.RecordingProgress
import app.myzel394.alibi.ui.components.RecorderScreen.atoms.RecordingTime
import app.myzel394.alibi.ui.components.RecorderScreen.atoms.SaveButton
import app.myzel394.alibi.ui.components.RecorderScreen.molecules.MicrophoneStatus
import app.myzel394.alibi.ui.components.RecorderScreen.molecules.RecordingControl
import app.myzel394.alibi.ui.components.RecorderScreen.molecules.RecordingStatus
import app.myzel394.alibi.ui.models.AudioRecorderModel
import app.myzel394.alibi.ui.utils.KeepScreenOn
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@Composable
fun AudioRecordingStatus(
    audioRecorder: AudioRecorderModel,
) {
    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    var now by remember { mutableStateOf(LocalDateTime.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(900)
        }
    }

    KeepScreenOn()
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Box {}
        RealtimeAudioVisualizer(audioRecorder = audioRecorder)

        RecordingStatus(
            recordingTime = audioRecorder.recordingTime!!,
            progress = audioRecorder.progress,
        )

        RecordingControl(
            isPaused = audioRecorder.isPaused,
            onDelete = {
                scope.launch {
                    runCatching {
                        audioRecorder.stopRecording(context)
                    }
                    runCatching {
                        audioRecorder.destroyService(context)
                    }
                    audioRecorder.batchesFolder!!.deleteRecordings()
                }
            },
            onPauseResume = {
                if (audioRecorder.isPaused) {
                    audioRecorder.resumeRecording()
                } else {
                    audioRecorder.pauseRecording()
                }
            },
            onSave = {
                audioRecorder.onRecordingSave(false)
            }
        )

        MicrophoneStatus(audioRecorder)
    }
}