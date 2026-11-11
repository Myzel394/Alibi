package app.myzel394.alibi.ui.components.RecorderScreen.molecules

import CameraSelectionButton
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import app.myzel394.alibi.ui.models.VideoRecorderModel
import app.myzel394.alibi.ui.utils.CameraInfo

@Composable
fun CamerasSelection(cameras: Iterable<CameraInfo>, videoSettings: VideoRecorderModel) {
    val context = LocalContext.current
    Column {
        cameras.filter { it.lens == CameraInfo.Lens.BACK || it.lens == CameraInfo.Lens.FRONT }.forEach { camera ->
            CameraSelectionButton(
                cameraID = camera.lens,
                selected = videoSettings.cameraSelection == camera.selection ||
                    (videoSettings.cameraSelection == null && camera.defaultForFacing && videoSettings.lensFacing == camera.lens.androidValue),
                onSelected = { videoSettings.selectCamera(camera) },
                label = camera.label(context),
            )
        }
    }
}
