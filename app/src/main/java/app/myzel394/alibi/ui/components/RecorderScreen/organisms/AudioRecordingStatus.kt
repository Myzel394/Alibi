package app.myzel394.alibi.ui.components.RecorderScreen.organisms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
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
import app.myzel394.alibi.ui.components.RecorderScreen.atoms.RealtimeAudioVisualizer
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
            .fillMaxSize()
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Box {}
        RealtimeAudioVisualizer(audioRecorder = audioRecorder)

        RecordingStatus(
            recordingTime = audioRecorder.recordingTime,
            progress = audioRecorder.progress,
            recordingStart = audioRecorder.recordingStart,
            maxDuration = audioRecorder.settings!!.maxDuration,
        )

        Column(
            verticalArrangement = Arrangement
                .spacedBy(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            MicrophoneStatus(audioRecorder)

            Divider()

            RecordingControl(
                isPaused = audioRecorder.isPaused,
                recordingTime = audioRecorder.recordingTime,
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
        }
    }
}