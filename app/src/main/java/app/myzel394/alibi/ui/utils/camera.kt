package app.myzel394.alibi.ui.utils

import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Rect
import android.hardware.camera2.CameraCharacteristics
import android.media.Image
import android.util.Size
import java.nio.ByteBuffer
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

// https://stackoverflow.com/a/55544614/9878135
fun imageToByteBuffer(image: Image): ByteBuffer {
    val crop: Rect = image.cropRect
    val width: Int = crop.width()
    val height: Int = crop.height()
    val planes = image.planes
    val rowData = ByteArray(planes[0].rowStride)
    val bufferSize = width * height * ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888) / 8
    val output = ByteBuffer.allocateDirect(bufferSize)
    var channelOffset = 0
    var outputStride = 0
    for (planeIndex in 0..2) {
        if (planeIndex == 0) {
            channelOffset = 0
            outputStride = 1
        } else if (planeIndex == 1) {
            channelOffset = width * height + 1
            outputStride = 2
        } else if (planeIndex == 2) {
            channelOffset = width * height
            outputStride = 2
        }
        val buffer = planes[planeIndex].buffer
        val rowStride = planes[planeIndex].rowStride
        val pixelStride = planes[planeIndex].pixelStride
        val shift = if (planeIndex == 0) 0 else 1
        val widthShifted = width shr shift
        val heightShifted = height shr shift
        buffer.position(rowStride * (crop.top shr shift) + pixelStride * (crop.left shr shift))
        for (row in 0 until heightShifted) {
            val length: Int
            if (pixelStride == 1 && outputStride == 1) {
                length = widthShifted
                buffer[output.array(), channelOffset, length]
                channelOffset += length
            } else {
                length = (widthShifted - 1) * pixelStride + 1
                buffer[rowData, 0, length]
                for (col in 0 until widthShifted) {
                    output.array()[channelOffset] = rowData[col * pixelStride]
                    channelOffset += outputStride
                }
            }
            if (row < heightShifted - 1) {
                buffer.position(buffer.position() + rowStride - length)
            }
        }
    }
    return output
}


// The following code is from https://stackoverflow.com/a/10028267/9878135
/**
 * Stack Blur v1.0 from
 * http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
 * Java Author: Mario Klingemann <mario at quasimondo.com>
 * http://incubator.quasimondo.com
 *
 * created Feburary 29, 2004
 * Android port : Yahel Bouaziz <yahel at kayenko.com>
 * http://www.kayenko.com
 * ported april 5th, 2012
 *
 * This is a compromise between Gaussian Blur and Box blur
 * It creates much better looking blurs than Box Blur, but is
 * 7x faster than my Gaussian Blur implementation.
 *
 * I called it Stack Blur because this describes best how this
 * filter works internally: it creates a kind of moving stack
 * of colors whilst scanning through the image. Thereby it
 * just has to add one new block of color to the right side
 * of the stack and remove the leftmost color. The remaining
 * colors on the topmost layer of the stack are either added on
 * or reduced by one, depending on if they are on the right or
 * on the left side of the stack.
 *
 * If you are using this algorithm in your code please add
 * the following line:
 * Stack Blur Algorithm by Mario Klingemann <mario></mario>@quasimondo.com>
</yahel></mario> */
fun fastblur(sentBitmap: Bitmap, scale: Float, radius: Int): Bitmap {
    assert(radius >= 1) { "radius must be >= 1" }

    var sentBitmap = sentBitmap
    val width = Math.round(sentBitmap.width * scale)
    val height = Math.round(sentBitmap.height * scale)
    sentBitmap = Bitmap.createScaledBitmap(sentBitmap, width, height, false)
    val bitmap = sentBitmap.copy(sentBitmap.config, true)
    val w = bitmap.width
    val h = bitmap.height
    val pix = IntArray(w * h)
    bitmap.getPixels(pix, 0, w, 0, 0, w, h)
    val wm = w - 1
    val hm = h - 1
    val wh = w * h
    val div = radius + radius + 1
    val r = IntArray(wh)
    val g = IntArray(wh)
    val b = IntArray(wh)
    var rsum: Int
    var gsum: Int
    var bsum: Int
    var x: Int
    var y: Int
    var i: Int
    var p: Int
    var yp: Int
    var yi: Int
    var yw: Int
    val vmin = IntArray(Math.max(w, h))
    var divsum = div + 1 shr 1
    divsum *= divsum
    val dv = IntArray(256 * divsum)
    i = 0
    while (i < 256 * divsum) {
        dv[i] = i / divsum
        i++
    }
    yi = 0
    yw = yi
    val stack = Array(div) { IntArray(3) }
    var stackpointer: Int
    var stackstart: Int
    var sir: IntArray
    var rbs: Int
    val r1 = radius + 1
    var routsum: Int
    var goutsum: Int
    var boutsum: Int
    var rinsum: Int
    var ginsum: Int
    var binsum: Int
    y = 0
    while (y < h) {
        bsum = 0
        gsum = bsum
        rsum = gsum
        boutsum = rsum
        goutsum = boutsum
        routsum = goutsum
        binsum = routsum
        ginsum = binsum
        rinsum = ginsum
        i = -radius
        while (i <= radius) {
            p = pix[yi + Math.min(wm, Math.max(i, 0))]
            sir = stack[i + radius]
            sir[0] = p and 0xff0000 shr 16
            sir[1] = p and 0x00ff00 shr 8
            sir[2] = p and 0x0000ff
            rbs = r1 - Math.abs(i)
            rsum += sir[0] * rbs
            gsum += sir[1] * rbs
            bsum += sir[2] * rbs
            if (i > 0) {
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
            } else {
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
            }
            i++
        }
        stackpointer = radius
        x = 0
        while (x < w) {
            r[yi] = dv[rsum]
            g[yi] = dv[gsum]
            b[yi] = dv[bsum]
            rsum -= routsum
            gsum -= goutsum
            bsum -= boutsum
            stackstart = stackpointer - radius + div
            sir = stack[stackstart % div]
            routsum -= sir[0]
            goutsum -= sir[1]
            boutsum -= sir[2]
            if (y == 0) {
                vmin[x] = Math.min(x + radius + 1, wm)
            }
            p = pix[yw + vmin[x]]
            sir[0] = p and 0xff0000 shr 16
            sir[1] = p and 0x00ff00 shr 8
            sir[2] = p and 0x0000ff
            rinsum += sir[0]
            ginsum += sir[1]
            binsum += sir[2]
            rsum += rinsum
            gsum += ginsum
            bsum += binsum
            stackpointer = (stackpointer + 1) % div
            sir = stack[stackpointer % div]
            routsum += sir[0]
            goutsum += sir[1]
            boutsum += sir[2]
            rinsum -= sir[0]
            ginsum -= sir[1]
            binsum -= sir[2]
            yi++
            x++
        }
        yw += w
        y++
    }
    x = 0
    while (x < w) {
        bsum = 0
        gsum = bsum
        rsum = gsum
        boutsum = rsum
        goutsum = boutsum
        routsum = goutsum
        binsum = routsum
        ginsum = binsum
        rinsum = ginsum
        yp = -radius * w
        i = -radius
        while (i <= radius) {
            yi = Math.max(0, yp) + x
            sir = stack[i + radius]
            sir[0] = r[yi]
            sir[1] = g[yi]
            sir[2] = b[yi]
            rbs = r1 - Math.abs(i)
            rsum += r[yi] * rbs
            gsum += g[yi] * rbs
            bsum += b[yi] * rbs
            if (i > 0) {
                rinsum += sir[0]
                ginsum += sir[1]
                binsum += sir[2]
            } else {
                routsum += sir[0]
                goutsum += sir[1]
                boutsum += sir[2]
            }
            if (i < hm) {
                yp += w
            }
            i++
        }
        yi = x
        stackpointer = radius
        y = 0
        while (y < h) {

            // Preserve alpha channel: ( 0xff000000 & pix[yi] )
            pix[yi] = -0x1000000 and pix[yi] or (dv[rsum] shl 16) or (dv[gsum] shl 8) or dv[bsum]
            rsum -= routsum
            gsum -= goutsum
            bsum -= boutsum
            stackstart = stackpointer - radius + div
            sir = stack[stackstart % div]
            routsum -= sir[0]
            goutsum -= sir[1]
            boutsum -= sir[2]
            if (x == 0) {
                vmin[y] = Math.min(y + r1, hm) * w
            }
            p = x + vmin[y]
            sir[0] = r[p]
            sir[1] = g[p]
            sir[2] = b[p]
            rinsum += sir[0]
            ginsum += sir[1]
            binsum += sir[2]
            rsum += rinsum
            gsum += ginsum
            bsum += binsum
            stackpointer = (stackpointer + 1) % div
            sir = stack[stackpointer]
            routsum += sir[0]
            goutsum += sir[1]
            boutsum += sir[2]
            rinsum -= sir[0]
            ginsum -= sir[1]
            binsum -= sir[2]
            yi += w
            y++
        }
        x++
    }
    bitmap.setPixels(pix, 0, w, 0, 0, w, h)
    return bitmap
}
