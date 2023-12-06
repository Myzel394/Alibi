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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
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
import app.myzel394.alibi.ui.components.RecorderScreen.atoms.RecordingProgress
import app.myzel394.alibi.ui.components.RecorderScreen.atoms.RecordingTime
import app.myzel394.alibi.ui.components.RecorderScreen.atoms.SaveButton
import app.myzel394.alibi.ui.components.RecorderScreen.atoms.TorchStatus
import app.myzel394.alibi.ui.components.RecorderScreen.molecules.RecordingControl
import app.myzel394.alibi.ui.components.RecorderScreen.molecules.RecordingStatus
import app.myzel394.alibi.ui.models.VideoRecorderModel
import app.myzel394.alibi.ui.utils.KeepScreenOn
import kotlinx.coroutines.launch

@Composable
fun VideoRecordingStatus(
    videoRecorder: VideoRecorderModel,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    KeepScreenOn()
    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Box {}

        Column {
            Icon(
                Icons.Default.CameraAlt,
                contentDescription = null,
                modifier = Modifier.size(64.dp)
            )
        }

        RecordingStatus(
            recordingTime = videoRecorder.recordingTime!!,
            progress = videoRecorder.progress,
        )

        Column(
            verticalArrangement = Arrangement
                .spacedBy(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 32.dp),
        ) {
            val cameraControl = videoRecorder.recorderService!!.cameraControl!!
            if (cameraControl.hasTorchAvailable()) {
                val isTorchEnabled = cameraControl.isTorchEnabled()

                TorchStatus(
                    enabled = isTorchEnabled,
                    onChange = {
                        if (isTorchEnabled) {
                            cameraControl.disableTorch()
                        } else {
                            cameraControl.enableTorch()
                        }
                    },
                )
            }

            Divider()

            RecordingControl(
                isPaused = videoRecorder.isPaused,
                onDelete = {
                    scope.launch {
                        runCatching {
                            videoRecorder.stopRecording(context)
                        }
                        videoRecorder.batchesFolder!!.deleteRecordings()
                    }
                },
                onPauseResume = {
                    if (videoRecorder.isPaused) {
                        videoRecorder.resumeRecording()
                    } else {
                        videoRecorder.pauseRecording()
                    }
                },
                onSave = {
                    scope.launch {
                        videoRecorder.stopRecording(context)
                        videoRecorder.onRecordingSave()
                    }
                }
            )
        }
    }
}