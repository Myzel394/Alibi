package app.myzel394.alibi.ui.utils

import android.hardware.camera2.CameraCharacteristics
import android.util.Size
import kotlin.math.abs

/**
 * Computes rotation required to transform the camera sensor output orientation to the
 * device's current orientation in degrees.
 *
 * @param characteristics The CameraCharacteristics to query for the sensor orientation.
 * @param surfaceRotationDegrees The current device orientation as a Surface constant.
 * @return Relative rotation of the camera sensor output.
 */
public fun computeRelativeRotation(
    characteristics: CameraCharacteristics,
    surfaceRotationDegrees: Int
): Int {
    val sensorOrientationDegrees =
        characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)!!

    // Reverse device orientation for back-facing cameras.
    val sign = if (characteristics.get(CameraCharacteristics.LENS_FACING) ==
        CameraCharacteristics.LENS_FACING_FRONT
    ) 1 else -1

    // Calculate desired orientation relative to camera orientation to make
    // the image upright relative to the device orientation.
    return (sensorOrientationDegrees - surfaceRotationDegrees * sign + 360) % 360
}

fun getOptimalPreviewSize(sizes: Array<Size>, w: Int, h: Int): Size {
    val ASPECT_TOLERANCE = 0.1
    val targetRatio = h.toDouble() / w
    var optimalSize: Size? = null
    var minDiff = Double.MAX_VALUE

    for (size in sizes) {
        val ratio = size.width / size.height
        if (abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue
        if (abs(size.height - h) < minDiff) {
            optimalSize = size
            minDiff = abs(size.height - h).toDouble()
        }
    }

    if (optimalSize == null) {
        minDiff = Double.MAX_VALUE
        for (size in sizes) {
            if (abs(size.height - h) < minDiff) {
                optimalSize = size
                minDiff = abs(size.height - h).toDouble()
            }
        }
    }
    return optimalSize!!
}