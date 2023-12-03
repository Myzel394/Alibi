package app.myzel394.alibi.ui.components.AudioRecorder.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.myzel394.alibi.R
import app.myzel394.alibi.ui.components.AudioRecorder.atoms.CameraSelectionButton
import app.myzel394.alibi.ui.models.VideoRecorderSettingsModel
import app.myzel394.alibi.ui.utils.CameraInfo

@Composable
fun CamerasSelection(
    cameras: Iterable<CameraInfo>,
    videoSettings: VideoRecorderSettingsModel
) {
    val CAMERA_LENS_TEXT_MAP = mapOf(
        CameraInfo.Lens.BACK to stringResource(R.string.ui_videoRecorder_action_start_settings_cameraLens_back_label),
        CameraInfo.Lens.FRONT to stringResource(R.string.ui_videoRecorder_action_start_settings_cameraLens_front_label),
        CameraInfo.Lens.EXTERNAL to stringResource(R.string.ui_videoRecorder_action_start_settings_cameraLens_external_label),
    )

    Column {
        if (cameras.count() == 2 && cameras.elementAt(0).id == 0 && cameras.elementAt(1).id == 1) {
            CameraSelectionButton(
                cameraID = CameraInfo.Lens.BACK,
                label = stringResource(R.string.ui_videoRecorder_action_start_settings_cameraLens_back_label),
                selected = videoSettings.cameraID == 0,
                onSelected = {
                    videoSettings.cameraID = 0
                },
            )
            CameraSelectionButton(
                cameraID = CameraInfo.Lens.FRONT,
                label = stringResource(R.string.ui_videoRecorder_action_start_settings_cameraLens_front_label),
                selected = videoSettings.cameraID == 1,
                onSelected = {
                    videoSettings.cameraID = 1
                },
            )
        } else {
            cameras.forEach { camera ->
                CameraSelectionButton(
                    cameraID = CameraInfo.CAMERA_INT_TO_LENS_MAP[camera.id]!!,
                    selected = videoSettings.cameraID == camera.id,
                    onSelected = {
                        videoSettings.cameraID = camera.id
                    },
                    label = stringResource(
                        R.string.ui_videoRecorder_action_start_settings_cameraLens_label,
                        camera.id
                    ),
                    description = CAMERA_LENS_TEXT_MAP[camera.lens]!!,
                )
            }
        }
    }
}