package app.myzel394.alibi.ui.utils

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata

data class CameraInfo(
    val lens: Lens,
    val id: Int,
) {
    enum class Lens(val androidValue: Int) {
        BACK(CameraMetadata.LENS_FACING_BACK),
        FRONT(CameraMetadata.LENS_FACING_FRONT),
        EXTERNAL(CameraMetadata.LENS_FACING_EXTERNAL),
    }

    companion object {
        val CAMERA_INT_TO_LENS_MAP = mapOf(
            0 to Lens.BACK,
            1 to Lens.FRONT,
            2 to Lens.EXTERNAL,
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
                lens = CAMERA_INT_TO_LENS_MAP[lensFacing]!!,
                id = cameraId.toInt(),
            )
        }
    }
}