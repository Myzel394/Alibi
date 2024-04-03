package app.myzel394.alibi.ui.components.RecorderScreen.organisms

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.ui.components.RecorderScreen.atoms.RealtimeAudioVisualizer
import app.myzel394.alibi.ui.components.RecorderScreen.atoms.SaveCurrentNowModal
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
    val configuration = LocalConfiguration.current.orientation

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
        RealtimeAudioVisualizer(
            audioRecorder = audioRecorder,
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 300.dp)
                .weight(1f),
        )

        when (configuration) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Column(
                        verticalArrangement = Arrangement
                            .spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(3f),
                    ) {
                        RecordingStatus(
                            recordingTime = audioRecorder.recordingTime,
                            progress = audioRecorder.progress,
                            recordingStart = audioRecorder.recordingStart,
                            maxDuration = audioRecorder.settings!!.maxDuration,
                            progressModifier = Modifier.fillMaxWidth(.9f),
                        )

                        MicrophoneStatus(audioRecorder)
                    }

                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        _PrimitiveControls(audioRecorder)
                    }
                }
            }

            else -> {
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

                    HorizontalDivider()

                    _PrimitiveControls(audioRecorder)
                }
            }
        }
    }
}

@Composable
fun _PrimitiveControls(audioRecorder: AudioRecorderModel) {
    val context = LocalContext.current
    val dataStore = context.dataStore
    val scope = rememberCoroutineScope()

    var showConfirmSaveNow by remember { mutableStateOf(false) }

    if (showConfirmSaveNow) {
        SaveCurrentNowModal(
            onDismiss = {
                showConfirmSaveNow = false
            },
            onConfirm = {
                showConfirmSaveNow = false

                scope.launch {
                    audioRecorder.recorderService!!.startNewCycle()

                    audioRecorder.onRecordingSave(false).join()
                }
            },
        )
    }

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
        onSaveAndStop = {
            scope.launch {
                audioRecorder.stopRecording(context)

                dataStore.updateData {
                    it.saveLastRecording(audioRecorder as RecorderModel)
                }

                audioRecorder.onRecordingSave(false).join()

                runCatching {
                    audioRecorder.destroyService(context)
                }
            }
        },
        onSaveCurrent = {
            showConfirmSaveNow = true
        },
    )
}