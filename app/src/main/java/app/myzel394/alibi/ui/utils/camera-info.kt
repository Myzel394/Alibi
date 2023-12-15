package app.myzel394.alibi.ui.utils

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalLensFacing

@OptIn(ExperimentalLensFacing::class)
data class CameraInfo(
    val id: Int,
) {
    enum class Lens(val androidValue: Int) {
        BACK(CameraSelector.LENS_FACING_BACK),
        FRONT(CameraSelector.LENS_FACING_FRONT),
        EXTERNAL(CameraSelector.LENS_FACING_EXTERNAL),
    }

    val lens: Lens
        get() = CAMERA_INT_TO_LENS_MAP[id]!!

    companion object {
        val CAMERA_INT_TO_LENS_MAP = mapOf(
            CameraSelector.LENS_FACING_BACK to Lens.BACK,
            CameraSelector.LENS_FACING_FRONT to Lens.FRONT,
            CameraSelector.LENS_FACING_EXTERNAL to Lens.EXTERNAL,
        )

        fun queryAvailableCameras(context: Context): List<CameraInfo> {
            val camera = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

            return camera.cameraIdList.map { id ->
                val lensFacing =
                    camera.getCameraCharacteristics(id).get(CameraCharacteristics.LENS_FACING)
                        ?: return@map null

                fromCameraId(id, lensFacing)
            }.filterNotNull()
        }

        fun fromCameraId(cameraId: String, lensFacing: Int): CameraInfo {
            return CameraInfo(
                id = cameraId.toInt(),
            )
        }

        // "normal cameras" means the device has a front and back camera
        fun checkHasNormalCameras(cameras: Iterable<CameraInfo>) =
            cameras.count() == 2 && cameras.elementAt(0).id == 0 && cameras.elementAt(1).id == 1
    }
}