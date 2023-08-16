package app.myzel394.alibi.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.params.SessionConfiguration
import android.media.Image
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController.VIDEO_CAPTURE
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.navigation.NavController
import app.myzel394.alibi.CameraHandler
import app.myzel394.alibi.R
import app.myzel394.alibi.ui.components.RecorderScreen.molecules.AudioRecordingStatus
import app.myzel394.alibi.ui.components.RecorderScreen.molecules.StartRecording
import app.myzel394.alibi.ui.components.RecorderScreen.molecules.VideoRecordingStatus
import app.myzel394.alibi.ui.enums.Screen
import app.myzel394.alibi.ui.models.AudioRecorderModel
import app.myzel394.alibi.ui.models.VideoRecorderModel
import com.ujizin.camposer.CameraPreview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.*
import java.io.File

@SuppressLint("MissingPermission", "NewApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecorderScreen(
    navController: NavController,
    audioRecorder: AudioRecorderModel,
    videoRecorder: VideoRecorderModel,
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.app_name))
                },
                actions = {
                    IconButton(
                        onClick = {
                            navController.navigate(Screen.Settings.route)
                        },
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = null
                        )
                    }
                }
            )
        },
    ) { padding ->
        val scope = rememberCoroutineScope()

        // Wait for `CameraHadler.openCamera`
        var camera by remember { mutableStateOf<CameraHandler?>(null) }

        Button(
            modifier = Modifier
                .padding(padding),
            onClick = {
            scope.launch {
                if (camera == null) {
                    camera = CameraHandler.Companion.openCamera(context)
                }

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
}
