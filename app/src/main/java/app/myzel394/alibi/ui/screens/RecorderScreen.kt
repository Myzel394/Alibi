package app.myzel394.alibi.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Point
import android.hardware.camera2.CameraCharacteristics
import android.hardware.display.DisplayManager
import android.os.Build
import android.util.Size
import android.view.Display
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewGroup
import android.view.WindowManager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import app.myzel394.alibi.CameraHandler
import app.myzel394.alibi.ui.models.AudioRecorderModel
import app.myzel394.alibi.ui.models.VideoRecorderModel
import app.myzel394.alibi.ui.utils.getOptimalPreviewSize
import kotlinx.coroutines.launch
import java.io.File
import java.lang.Float.max

@SuppressLint("MissingPermission", "NewApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecorderScreen(
    navController: NavController,
    audioRecorder: AudioRecorderModel,
    videoRecorder: VideoRecorderModel,
) {
    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    var camera by remember { mutableStateOf<CameraHandler?>(null) }

    LaunchedEffect(Unit) {
        camera = CameraHandler.openCamera(context)
    }

    if (camera == null) {
        return
    } else {
        var scaleValue by remember { mutableFloatStateOf(1f) }

        val screenSize = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val bounds = windowManager.currentWindowMetrics.bounds

            Size(bounds.width(), bounds.height())
        } else {
            val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
            val display = displayManager.getDisplay(Display.DEFAULT_DISPLAY)

            val size = Point()
            display.getRealSize(size)

            Size(size.x, size.y)
        }

        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .scale(scaleValue),
            factory = { context ->
                val surface = object : SurfaceView(context) {
                    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
                        val width = resolveSize(suggestedMinimumWidth, widthMeasureSpec);
                        val height = resolveSize(suggestedMinimumHeight, heightMeasureSpec);

                        val supportedPreviewSizes = camera!!
                            .characteristics
                            .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
                            .getOutputSizes(SurfaceHolder::class.java)

                        // Make sure preview is in `cover` mode and not `contain` mode

                        if (supportedPreviewSizes != null) {
                            val previewSize = getOptimalPreviewSize(supportedPreviewSizes, width, height);

                            val ratio = if (previewSize.height >= previewSize.width)
                                    (previewSize.height / previewSize.width).toFloat()
                                else (previewSize.width / previewSize.height).toFloat()

                            val optimalWidth = width
                            val optimalHeight = (width * ratio).toInt()

                            // Make sure the camera preview uses the whole screen
                            val widthScaleRatio = optimalWidth.toFloat() / previewSize.width
                            val heightScaleUpRatio = optimalHeight.toFloat() / previewSize.height

                            setMeasuredDimension(
                                (previewSize.width * widthScaleRatio).toInt(),
                                (previewSize.height * heightScaleUpRatio).toInt()
                            )

                            scaleValue = max(
                                screenSize.width / optimalWidth.toFloat(),
                                screenSize.height / optimalHeight.toFloat(),
                            )
                        }
                    }
                }

                val surfaceView = surface.apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                }

                surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
                    override fun surfaceCreated(holder: SurfaceHolder) {
                        scope.launch {
                            camera!!.startPreview(holder.surface)
                        }
                    }

                    override fun surfaceChanged(
                        holder: SurfaceHolder,
                        format: Int,
                        width: Int,
                        height: Int
                    ) {
                    }

                    override fun surfaceDestroyed(holder: SurfaceHolder) {
                        scope.launch {
                            camera!!.stopPreview()
                        }
                    }
                })

                surfaceView
            }

        )
        Button(
            onClick = {
                scope.launch {
                    camera!!.takePhoto(
                        File(
                            context.externalCacheDir!!.absolutePath,
                            "test.jpg"
                        )
                    )
                }
            }) {
            Text("Take photo")
        }
    }

        /*
        LaunchedEffect(Unit) {
            val cameraManager = getSystemService(context, CameraManager::class.java)!!
            val backgroundThread = HandlerThread("CameraVideo").apply {
                start()
            }
            val backgroundHandler = Handler(backgroundThread.looper)

            val characteristics = cameraManager.getCameraCharacteristics(CameraCharacteristics.LENS_FACING_BACK.toString())


            val cameraStateCallback = object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    val captureStateCallback = object : CameraCaptureSession.StateCallback() {
                        override fun onConfigureFailed(session: CameraCaptureSession) {
                            Log.e("Camera", "Failed to configure camera")
                        }
                        override fun onConfigured(session: CameraCaptureSession) {
                            val onImageAvailableListener = object: ImageReader.OnImageAvailableListener{
                                override fun onImageAvailable(reader: ImageReader) {
                                    val image: Image = reader.acquireLatestImage()
                                }
                            }
                            val captureRequest = camera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)

                            session.capture(captureRequest.build(), null, backgroundHandler)
                        }
                    }

                    val previewSize = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!.getOutputSizes(ImageFormat.JPEG).maxByOrNull { it.height * it.width }!!
                    val imageReader = ImageReader.newInstance(previewSize.width, previewSize.height, ImageFormat.JPEG, 1)
                    camera.createCaptureSession(
                        listOf(imageReader.surface),
                        captureStateCallback,
                        backgroundHandler
                    )
                }

                override fun onDisconnected(cameraDevice: CameraDevice) {

                }

                override fun onError(cameraDevice: CameraDevice, error: Int) {
                    val errorMsg = when(error) {
                        ERROR_CAMERA_DEVICE -> "Fatal (device)"
                        ERROR_CAMERA_DISABLED -> "Device policy"
                        ERROR_CAMERA_IN_USE -> "Camera in use"
                        ERROR_CAMERA_SERVICE -> "Fatal (service)"
                        ERROR_MAX_CAMERAS_IN_USE -> "Maximum cameras in use"
                        else -> "Unknown"
                    }
                }
            }

            cameraManager.openCamera(
                CameraCharacteristics.LENS_FACING_BACK.toString(),
                cameraStateCallback,
                backgroundHandler
            )
        }
         */
}
