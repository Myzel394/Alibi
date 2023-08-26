package app.myzel394.alibi

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.media.ImageReader
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicYuvToRGB
import android.util.Log
import android.util.Size
import android.view.Surface
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.getSystemService
import app.myzel394.alibi.ui.utils.fastblur
import app.myzel394.alibi.ui.utils.getScreenSize
import app.myzel394.alibi.ui.utils.imageToByteBuffer
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.nio.ByteBuffer
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


class CameraHandler(
    private val manager: CameraManager,
    private val device: CameraDevice,
    private val handler: Handler,
    private val thread: HandlerThread,
    private val lens: Int = CameraCharacteristics.LENS_FACING_BACK,
) {
    private lateinit var imageReader: ImageReader

    val characteristics: CameraCharacteristics
        get() = manager.getCameraCharacteristics(lens.toString())

    @RequiresApi(Build.VERSION_CODES.P)
    private suspend fun createCaptureSession(
        outputs: List<Surface>,
    ): CameraCaptureSession = suspendCancellableCoroutine { cont ->
        // I really don't want to use a deprecated method, but there is
        // absolutely no documentation available for the new method.
        device.createCaptureSession(
            outputs,
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    cont.resume(session)
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    cont.resumeWithException(RuntimeException("Failed to configure session"))
                }
            },
            handler,
        )
    }

    @RequiresApi(Build.VERSION_CODES.P)
    suspend fun startPreview(
        surface: Surface,
    ) {
        val outputs = listOf(surface)
        val session = createCaptureSession(outputs)

        val captureRequest = device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        captureRequest.addTarget(surface)

        session.setRepeatingRequest(
            captureRequest.build(),
            null,
            handler,
        )
    }

    @RequiresApi(Build.VERSION_CODES.P)
    suspend fun startBlurredPreview(
        surface: Surface,
        context: Context,
        size: Size? = null,
    ) {
        val readerSize = size ?: getScreenSize(context).let {
            Size(
                it.width / 10,
                it.height / 10,
            )
        }
        val imageReader = ImageReader.newInstance(
            readerSize.width,
            readerSize.height,
            ImageFormat.YUV_420_888,
            IMAGE_BUFFER_SIZE,
        )
        imageReader.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage() ?: return@setOnImageAvailableListener

            val yuvBytes: ByteBuffer = imageToByteBuffer(image)

            // Convert YUV to RGB
            val rs = RenderScript.create(context)

            val bitmap = Bitmap.createBitmap(image.width, image.height, Bitmap.Config.ARGB_8888)
            val allocationRgb = Allocation.createFromBitmap(rs, bitmap)

            val allocationYuv = Allocation.createSized(rs, Element.U8(rs), yuvBytes.array().size)
            allocationYuv.copyFrom(yuvBytes.array())

            val scriptYuvToRgb = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs))
            scriptYuvToRgb.setInput(allocationYuv)
            scriptYuvToRgb.forEach(allocationRgb)

            allocationRgb.copyTo(bitmap)

            // Rotate bitmap
            val matrix = android.graphics.Matrix()
            matrix.postRotate(90f)
            val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

            val blurredBitmap = fastblur(rotatedBitmap, 1f, 2)

            // Send blurredBitmap to `surface`
            val canvas = surface.lockCanvas(null)
            canvas.drawBitmap(blurredBitmap, 0f, 0f, null)
            surface.unlockCanvasAndPost(canvas)

            // Destroy
            allocationRgb.destroy()
            allocationYuv.destroy()
            scriptYuvToRgb.destroy()
            rs.destroy()
            bitmap.recycle()
            rotatedBitmap.recycle()
            blurredBitmap.recycle()

            // Release
            image.close()
        }, handler)

        val outputs = listOf(imageReader.surface)
        val session = createCaptureSession(outputs)

        val captureRequest = device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        captureRequest.addTarget(imageReader.surface)

        session.setRepeatingRequest(
            captureRequest.build(),
            null,
            handler,
        )
    }

    fun stopPreview() {
        device.close()
        thread.quitSafely()
    }

    @RequiresApi(Build.VERSION_CODES.P)
    suspend fun takePhoto(
        outputFile: File,
    ) {
        Log.d("Alibi", "Taking photo")

        Log.d("Alibi", "Creating Camera Characteristics")
        val characteristics = manager.getCameraCharacteristics(lens.toString())
        Log.d("Alibi", "Creating size")
        val size = characteristics.get(
            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
            .getOutputSizes(ImageFormat.JPEG).maxByOrNull { it.height * it.width }!!
        Log.d("Alibi", "Creating image reader")
        imageReader = ImageReader.newInstance(
            size.width,
            size.height,
            ImageFormat.JPEG,
            1,
        )

        imageReader.setOnImageAvailableListener({ reader ->
            val image = reader.acquireLatestImage()

            // Save image to file
            outputFile.outputStream().use { stream ->
                image.planes[0].buffer.apply {
                    val bytes = ByteArray(remaining())
                    get(bytes)
                    stream.write(bytes)
                }
            }

            println("Image saved to ${outputFile.absolutePath}")

            image.close()
        }, handler)

        Log.d("Alibi", "Creating capture session")
        val session = createCaptureSession(listOf(imageReader.surface))

        Log.d("Alibi", "Creating capture request")
        val captureRequest = device.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
        captureRequest.addTarget(imageReader.surface)

        Log.d("Alibi", "Capturing")
        session.capture(captureRequest.build(), null, handler)
        Log.d("Alibi", "Success!")
    }

    private fun getOptimalPreviewSize(sizes: List<Size>, w: Int, h: Int): Size {
        val ASPECT_TOLERANCE = 0.1
        val targetRatio = h.toDouble() / w
        var optimalSize: Size? = null

        var minDiff = Double.MAX_VALUE

        for (size in sizes) {
            val ratio: Double = size.width as Double / size.height
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue
            if (Math.abs(size.height - h) < minDiff) {
                optimalSize = size
                minDiff = Math.abs(size.height - h).toDouble()
            }
        }


        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE
            for (size in sizes) {
                if (Math.abs(size.height - h) < minDiff) {
                    optimalSize = size
                    minDiff = Math.abs(size.height - h).toDouble()
                }
            }
        }

        return optimalSize!!
    }

    fun getPreviewSize(): Size {
        return characteristics
            .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
            .getOutputSizes(ImageFormat.JPEG)
            .maxByOrNull { it.height * it.width }!!
    }

    companion object {
        const val IMAGE_BUFFER_SIZE = 2

        fun getCameraManager(
            context: Context,
        ): CameraManager = getSystemService(context, CameraManager::class.java)!!

        fun createThread(): HandlerThread = HandlerThread("CameraHandler").apply { start() }
        fun createHandler(thread: HandlerThread): Handler = Handler(thread.looper)

        @SuppressLint("MissingPermission")
        suspend fun openCamera(
            manager: CameraManager,
            cameraId: String,
            thread: HandlerThread,
        ): CameraHandler = suspendCancellableCoroutine { cont ->
            val handler = createHandler(thread)
            manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(device: CameraDevice) {
                    cont.resume(
                        CameraHandler(
                            manager,
                            device,
                            handler,
                            thread,
                        )
                    )

                }

                override fun onDisconnected(device: CameraDevice) {
                    Log.w("Alibi", "Camera $cameraId has been disconnected")
                }

                override fun onError(device: CameraDevice, error: Int) {
                    val msg = when (error) {
                        ERROR_CAMERA_DEVICE -> "Fatal (device)"
                        ERROR_CAMERA_DISABLED -> "Device policy"
                        ERROR_CAMERA_IN_USE -> "Camera in use"
                        ERROR_CAMERA_SERVICE -> "Fatal (service)"
                        ERROR_MAX_CAMERAS_IN_USE -> "Maximum cameras in use"
                        else -> "Unknown"
                    }
                    val exc = RuntimeException("Camera $cameraId error: ($error) $msg")
                    Log.e("Alibi", exc.message, exc)
                    if (cont.isActive) cont.resumeWithException(exc)
                }
            }, handler)
        }

        suspend fun openCamera(
            context: Context,
        ): CameraHandler {
            val manager = getCameraManager(context)
            val cameraId = manager.cameraIdList.first()
            val thread = createThread()
            return openCamera(manager, cameraId, thread)
        }
    }
}