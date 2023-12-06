package app.myzel394.alibi.ui.components.RecorderScreen.organisms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import app.myzel394.alibi.ui.components.RecorderScreen.atoms.DeleteButton
import app.myzel394.alibi.ui.components.RecorderScreen.atoms.PauseResumeButton
import app.myzel394.alibi.ui.components.RecorderScreen.atoms.SaveButton
import app.myzel394.alibi.ui.components.RecorderScreen.atoms.TorchStatus
import app.myzel394.alibi.ui.models.VideoRecorderModel
import kotlinx.coroutines.launch

@Composable
fun VideoRecordingStatus(
    videoRecorder: VideoRecorderModel,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Box {}

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
                        scope.launch {
                            videoRecorder.stopRecording(context)
                            videoRecorder.batchesFolder!!.deleteRecordings()
                        }
                    }
                )
            }

            Box(
                contentAlignment = Alignment.Center,
            ) {
                PauseResumeButton(
                    isPaused = videoRecorder.isPaused,
                    onChange = {
                        if (videoRecorder.isPaused) {
                            videoRecorder.resumeRecording()
                        } else {
                            videoRecorder.pauseRecording()
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
                        scope.launch {
                            runCatching {
                                videoRecorder.stopRecording(context)
                            }
                            videoRecorder.onRecordingSave()
                        }
                    }
                )
            }
        }

        val cameraControl = videoRecorder.recorderService!!.cameraControl!!
        println("cameraControl.hasTorchAvailable(): ${cameraControl.hasTorchAvailable()}")
        println("videoRecorder: ${videoRecorder.recorderService?.cameraControl}")
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

        Box {}
    }
}