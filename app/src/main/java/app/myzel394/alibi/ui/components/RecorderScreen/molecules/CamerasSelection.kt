package app.myzel394.alibi.ui.components.RecorderScreen.molecules

import CameraSelectionButton
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalLensFacing
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import app.myzel394.alibi.R
import app.myzel394.alibi.ui.models.VideoRecorderModel
import app.myzel394.alibi.ui.utils.CameraInfo

@Composable
fun CamerasSelection(
    cameras: Iterable<CameraInfo>,
    videoSettings: VideoRecorderModel,
) {
    val CAMERA_LENS_TEXT_MAP = mapOf(
        CameraInfo.Lens.BACK to stringResource(R.string.ui_videoRecorder_action_start_settings_cameraLens_back_label),
        CameraInfo.Lens.FRONT to stringResource(R.string.ui_videoRecorder_action_start_settings_cameraLens_front_label),
        CameraInfo.Lens.EXTERNAL to stringResource(R.string.ui_videoRecorder_action_start_settings_cameraLens_external_label),
    )

    Column {
        if (CameraInfo.checkHasNormalCameras(cameras)) {
            CameraSelectionButton(
                cameraID = CameraInfo.Lens.BACK,
                label = stringResource(R.string.ui_videoRecorder_action_start_settings_cameraLens_back_label),
                selected = videoSettings.cameraID == CameraInfo.Lens.BACK.androidValue,
                onSelected = {
                    videoSettings.cameraID = CameraInfo.Lens.BACK.androidValue
                },
            )
            CameraSelectionButton(
                cameraID = CameraInfo.Lens.FRONT,
                label = stringResource(R.string.ui_videoRecorder_action_start_settings_cameraLens_front_label),
                selected = videoSettings.cameraID == CameraInfo.Lens.FRONT.androidValue,
                onSelected = {
                    videoSettings.cameraID = CameraInfo.Lens.FRONT.androidValue
                },
            )
        } else {
            cameras.forEach { camera ->
                CameraSelectionButton(
                    cameraID = camera.lens,
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