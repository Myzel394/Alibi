package app.myzel394.alibi.ui.components.RecorderScreen.molecules

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.myzel394.alibi.R
import app.myzel394.alibi.ui.components.RecorderScreen.atoms.CameraSelectionButton
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