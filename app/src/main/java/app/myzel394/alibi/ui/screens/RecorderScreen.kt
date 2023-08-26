package app.myzel394.alibi.ui.screens

import android.annotation.SuppressLint
import android.hardware.camera2.CameraCharacteristics
import android.util.Size
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import app.myzel394.alibi.CameraHandler
import app.myzel394.alibi.ui.models.AudioRecorderModel
import app.myzel394.alibi.ui.models.VideoRecorderModel
import app.myzel394.alibi.ui.utils.ChangeNavColors
import app.myzel394.alibi.ui.utils.getOptimalPreviewSize
import app.myzel394.alibi.ui.utils.rememberScreenSize
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

        println("scaleValue: $scaleValue")

        val screenSize = rememberScreenSize()
        val previewSize = Size(
            screenSize.width / 10,
            screenSize.height / 10,
        )

        ChangeNavColors(color = Color.Transparent)

        AndroidView(
            modifier = Modifier
                .fillMaxSize(),
            factory = { context ->
                val surface = object : SurfaceView(context) {
                    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
                        setMeasuredDimension(
                            screenSize.width,
                            screenSize.height,
                        )
                    }
                }

                val surfaceView = surface.apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                }

                surfaceView.holder.apply {
                    addCallback(object : SurfaceHolder.Callback {
                        override fun surfaceCreated(holder: SurfaceHolder) {
                            scope.launch {
                                camera!!.startBlurredPreview(
                                    holder.surface,
                                    context,
                                    previewSize,
                                )
                            }
                        }

                        override fun surfaceChanged(
                            holder: SurfaceHolder,
                            format: Int,
                            width: Int,
                            height: Int
                        ) {
                            println("surfaceChanged")
                            println("width: $width, height: $height")
                        }

                        override fun surfaceDestroyed(holder: SurfaceHolder) {
                            scope.launch {
                                camera!!.stopPreview()
                            }
                        }
                    })
                    setFixedSize(previewSize.width, previewSize.height)
                }

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                )
        )
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
