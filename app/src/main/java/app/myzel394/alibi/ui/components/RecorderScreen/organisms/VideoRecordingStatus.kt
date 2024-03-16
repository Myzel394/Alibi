package app.myzel394.alibi.ui.components.RecorderScreen.organisms

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.myzel394.alibi.R
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.ui.components.RecorderScreen.atoms.TorchStatus
import app.myzel394.alibi.ui.components.RecorderScreen.molecules.RecordingControl
import app.myzel394.alibi.ui.components.RecorderScreen.molecules.RecordingStatus
import app.myzel394.alibi.ui.models.VideoRecorderModel
import app.myzel394.alibi.ui.utils.CameraInfo
import app.myzel394.alibi.ui.utils.KeepScreenOn
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.launch

@Composable
fun VideoRecordingStatus(
    videoRecorder: VideoRecorderModel,
) {
    val orientation = LocalConfiguration.current.orientation

    KeepScreenOn()

    when (orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement
                        .spacedBy(32.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(0.9f)
                        .align(Alignment.CenterVertically),
                ) {
                    _VideoGeneralInfo(videoRecorder)
                    _VideoRecordingStatus(videoRecorder)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(0.9f)
                ) {
                    Column(
                        verticalArrangement = Arrangement
                            .spacedBy(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        _VideoControls(videoRecorder)
                        HorizontalDivider()
                        _PrimitiveControls(videoRecorder)
                    }
                }
            }
        }

        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Box {}

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement
                        .spacedBy(16.dp),
                ) {
                    _VideoGeneralInfo(videoRecorder)
                    _VideoRecordingStatus(videoRecorder)
                }

                Column(
                    verticalArrangement = Arrangement
                        .spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    _VideoControls(videoRecorder)
                    HorizontalDivider()
                    _PrimitiveControls(videoRecorder)
                }
            }
        }
    }

}

@Composable
fun _VideoGeneralInfo(videoRecorder: VideoRecorderModel) {
    val context = LocalContext.current
    val availableCameras = CameraInfo.queryAvailableCameras(context)
    val orientation = LocalConfiguration.current.orientation

    Column(
        verticalArrangement = Arrangement
            .spacedBy(if (orientation == Configuration.ORIENTATION_LANDSCAPE) 12.dp else 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Default.CameraAlt,
            contentDescription = null,
            modifier = Modifier.size(if (orientation == Configuration.ORIENTATION_LANDSCAPE) 48.dp else 64.dp)
        )

        if (videoRecorder.isStartingRecording) {
            Box(
                modifier = Modifier
                    .width(128.dp)
                    .height(
                        with(LocalDensity.current) {
                            MaterialTheme.typography.labelMedium.fontSize.toDp()
                        }
                    )
                    .shimmer()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        MaterialTheme.shapes.small
                    )
            )
        } else {
            Text(
                stringResource(
                    R.string.form_value_selected,
                    if (CameraInfo.checkHasNormalCameras(availableCameras)) {
                        videoRecorder.cameraID.let {
                            if (it == CameraInfo.Lens.BACK.androidValue)
                                stringResource(R.string.ui_videoRecorder_action_start_settings_cameraLens_back_label)
                            else
                                stringResource(R.string.ui_videoRecorder_action_start_settings_cameraLens_front_label)
                        }
                    } else {
                        stringResource(
                            R.string.ui_videoRecorder_action_start_settings_cameraLens_label,
                            videoRecorder.cameraID
                        )
                    }
                ),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
fun _VideoRecordingStatus(videoRecorder: VideoRecorderModel) {
    RecordingStatus(
        recordingTime = videoRecorder.recordingTime,
        progress = videoRecorder.progress,
        recordingStart = videoRecorder.recordingStart,
        maxDuration = videoRecorder.settings!!.maxDuration,
    )
}

@Composable
fun _PrimitiveControls(videoRecorder: VideoRecorderModel) {
    val context = LocalContext.current
    val dataStore = context.dataStore
    val scope = rememberCoroutineScope()

    RecordingControl(
        orientation = Configuration.ORIENTATION_PORTRAIT,
        // There may be some edge cases where the app may crash if the
        // user stops or pauses the recording too soon, so we simply add a
        // small delay to prevent that
        initialDelay = 1000L,
        isPaused = videoRecorder.isPaused,
        recordingTime = videoRecorder.recordingTime,
        onDelete = {
            scope.launch {
                runCatching {
                    videoRecorder.stopRecording(context)
                }
                runCatching {
                    videoRecorder.destroyService(context)
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

                dataStore.updateData {
                    it.saveLastRecording(videoRecorder as RecorderModel)
                }

                videoRecorder.onRecordingSave()

                runCatching {
                    videoRecorder.destroyService(context)
                }
            }
        }
    )
}

@Composable
fun _VideoControls(videoRecorder: VideoRecorderModel) {
    if (!videoRecorder.isStartingRecording) {
        val cameraControl = videoRecorder.recorderService!!.cameraControl!!
        if (cameraControl.hasTorchAvailable()) {
            var torchEnabled by rememberSaveable { mutableStateOf(cameraControl.torchEnabled) }

            TorchStatus(
                enabled = torchEnabled,
                onChange = {
                    if (torchEnabled) {
                        torchEnabled = false
                        cameraControl.disableTorch()
                    } else {
                        torchEnabled = true
                        cameraControl.enableTorch()
                    }
                },
            )
        }
    }
}