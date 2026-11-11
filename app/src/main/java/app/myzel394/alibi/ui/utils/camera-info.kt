package app.myzel394.alibi.ui.utils

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalLensFacing
import app.myzel394.alibi.R
import app.myzel394.alibi.db.CameraSelection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.sqrt

@androidx.annotation.OptIn(ExperimentalLensFacing::class, ExperimentalCamera2Interop::class)
data class CameraInfo(
    val id: String,
    val lens: Lens,
    val physicalId: String? = null,
    val focalLengthMm: Float? = null,
    val equivalent35mm: Float? = null,
    val logicalGroup: Boolean = false,
    val defaultForFacing: Boolean = false,
) {
    enum class Lens(val androidValue: Int) {
        BACK(CameraSelector.LENS_FACING_BACK), FRONT(CameraSelector.LENS_FACING_FRONT),
        EXTERNAL(CameraSelector.LENS_FACING_EXTERNAL), UNKNOWN(999),
    }
    val selection get() = CameraSelection(id, physicalId)
    val selectionKey get() = "$id:${physicalId ?: "auto"}"
    fun label(context: Context): String {
        val facing = context.getString(when (lens) {
            Lens.BACK -> R.string.ui_videoRecorder_action_start_settings_cameraLens_back_label
            Lens.FRONT -> R.string.ui_videoRecorder_action_start_settings_cameraLens_front_label
            Lens.EXTERNAL -> R.string.ui_videoRecorder_action_start_settings_cameraLens_external_label
            Lens.UNKNOWN -> R.string.ui_videoRecorder_action_start_settings_cameraLens_unknown_label
        })
        val name = if (physicalId == null && logicalGroup) context.getString(R.string.camera_logical_group)
            else equivalent35mm?.let { context.getString(R.string.camera_equivalent_focal, it.toInt()) }
                ?: focalLengthMm?.let { context.getString(R.string.camera_focal_length, String.format(Locale.getDefault(), "%.1f", it)) }
                ?: context.getString(R.string.camera_lens)
        return "$facing · $name · ID ${physicalId ?: id}"
    }
    companion object {
        val CAMERA_INT_TO_LENS_MAP = mapOf(
            CameraSelector.LENS_FACING_BACK to Lens.BACK,
            CameraSelector.LENS_FACING_FRONT to Lens.FRONT,
            CameraSelector.LENS_FACING_EXTERNAL to Lens.EXTERNAL,
        )
        fun fromCameraId(cameraId: String, lensFacing: Int) = CameraInfo(cameraId, CAMERA_INT_TO_LENS_MAP[lensFacing] ?: Lens.UNKNOWN)
        fun queryAvailableCameras(context: Context): List<CameraInfo> {
            val manager = context.getSystemService(CameraManager::class.java)
            val result = mutableListOf<CameraInfo>()
            for (id in manager.cameraIdList) {
                runCatching {
                    val info = manager.getCameraCharacteristics(id)
                    val lens = CAMERA_INT_TO_LENS_MAP[info.get(CameraCharacteristics.LENS_FACING)] ?: Lens.UNKNOWN
                    val physicalIds = if (Build.VERSION.SDK_INT >= 28) info.physicalCameraIds else emptySet()
                    result += describe(id, lens, null, info, physicalIds.isNotEmpty())
                    for (physical in physicalIds.sorted()) runCatching {
                        val physicalInfo = manager.getCameraCharacteristics(physical)
                        result += describe(id, lens, physical, physicalInfo, false)
                    }
                }
            }
            return result.distinctBy { it.selection }.sortedWith(
                compareBy<CameraInfo> { it.lens.ordinal }.thenBy { it.id }.thenBy { it.physicalId != null }
                    .thenBy { it.equivalent35mm ?: it.focalLengthMm ?: Float.MAX_VALUE })
        }
        /** Filter through CameraX so system-only camera IDs are not advertised as selectable. */
        suspend fun querySelectableCameras(context: Context): List<CameraInfo> {
            val catalog = withContext(Dispatchers.IO) { queryAvailableCameras(context) }
            val provider = context.getCameraProvider()
            val available = provider.availableCameraInfos.map { Camera2CameraInfo.from(it).cameraId }.toSet()
            val defaults = listOf(CameraSelector.LENS_FACING_BACK, CameraSelector.LENS_FACING_FRONT).mapNotNull { lens ->
                runCatching { Camera2CameraInfo.from(provider.getCameraInfo(selector(lens, null))).cameraId }.getOrNull()
            }.toSet()
            return catalog.filter { it.id in available }.map { it.copy(defaultForFacing = it.physicalId == null && it.id in defaults) }
        }
        fun selector(lensFacing: Int, selection: CameraSelection?): CameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing).apply {
                if (selection != null) addCameraFilter { infos ->
                    infos.filter { Camera2CameraInfo.from(it).cameraId == selection.logicalId }
                }
            }.build()
        fun checkHasNormalCameras(cameras: Iterable<CameraInfo>) = cameras.count() == 2 &&
            cameras.any { it.lens == Lens.BACK } && cameras.any { it.lens == Lens.FRONT }
        private fun describe(id: String, lens: Lens, physical: String?, info: CameraCharacteristics, logical: Boolean): CameraInfo {
            val focal = info.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)?.minOrNull()
            val sensor = info.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE)
            val diagonal = sensor?.let { sqrt(it.width * it.width + it.height * it.height) }
            val equivalent = if (focal != null && diagonal != null && diagonal > 0) focal * 43.2666f / diagonal else null
            return CameraInfo(id, lens, physical, focal, equivalent, logical)
        }
    }
}
