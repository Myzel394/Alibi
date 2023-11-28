package app.myzel394.alibi.ui.components.AudioRecorder.organisms

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.ui.components.AudioRecorder.atoms.DeleteButton
import app.myzel394.alibi.ui.components.AudioRecorder.atoms.MicrophoneDisconnectedDialog
import app.myzel394.alibi.ui.components.AudioRecorder.atoms.MicrophoneReconnectedDialog
import app.myzel394.alibi.ui.components.AudioRecorder.atoms.PauseResumeButton
import app.myzel394.alibi.ui.components.AudioRecorder.atoms.RealtimeAudioVisualizer
import app.myzel394.alibi.ui.components.AudioRecorder.atoms.RecordingTime
import app.myzel394.alibi.ui.components.AudioRecorder.atoms.SaveButton
import app.myzel394.alibi.ui.components.AudioRecorder.molecules.MicrophoneSelection
import app.myzel394.alibi.ui.components.AudioRecorder.molecules.MicrophoneStatus
import app.myzel394.alibi.ui.effects.rememberPrevious
import app.myzel394.alibi.ui.models.AudioRecorderModel
import app.myzel394.alibi.ui.utils.KeepScreenOn
import app.myzel394.alibi.ui.utils.MicrophoneInfo
import kotlinx.coroutines.delay
import java.time.LocalDateTime

@Composable
fun RecordingStatus(
    audioRecorder: AudioRecorderModel,
) {
    val context = LocalContext.current

    var now by remember { mutableStateOf(LocalDateTime.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(900)
        }
    }

    // Only show animation when the recording has just started
    val recordingJustStarted = audioRecorder.recordingTime!! <= 1000L
    var progressVisible by remember { mutableStateOf(!recordingJustStarted) }
    LaunchedEffect(Unit) {
        progressVisible = true
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            RecordingTime(audioRecorder.recordingTime!!)
            Spacer(modifier = Modifier.height(16.dp))
            AnimatedVisibility(
                visible = progressVisible,
                enter = expandHorizontally(
                    tween(1000)
                )
            ) {
                LinearProgressIndicator(
                    progress = audioRecorder.progress,
                    modifier = Modifier
                        .width(300.dp)
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                DeleteButton(
                    onDelete = {
                        //audioRecorder.stopRecording(context)
                        audioRecorder.batchesFolder!!.deleteRecordings();
                    }
                )
            }

            Box(
                contentAlignment = Alignment.Center,
            ) {
                PauseResumeButton(
                    isPaused = audioRecorder.isPaused,
                    onChange = {
                        if (audioRecorder.isPaused) {
                            audioRecorder.resumeRecording()
                        } else {
                            audioRecorder.pauseRecording()
                        }
                    },
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                SaveButton(
                    onSave = {
                        runCatching {
                            //audioRecorder.stopRecording(context)
                        }
                        audioRecorder.onRecordingSave()
                    }
                )
            }
        }

        MicrophoneStatus(audioRecorder)
    }
}