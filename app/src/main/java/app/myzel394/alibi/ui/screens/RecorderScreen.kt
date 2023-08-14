package app.myzel394.alibi.ui.screens

import android.annotation.SuppressLint
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.params.SessionConfiguration
import android.media.Image
import android.media.ImageReader
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
import androidx.navigation.NavController
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
import java.io.File

@SuppressLint("MissingPermission")
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

    val cameraController = remember {LifecycleCameraController(context)}

    LaunchedEffect(Unit) {
        scope.launch {
            // Take a picture
            val manager = ContextCompat.getSystemService(context, android.hardware.camera2.CameraManager::class.java)!!

            val cameraId = manager.cameraIdList.first()

            manager.openCamera(
                cameraId,
                object: CameraDevice.StateCallback() {
                    override fun onDisconnected(p0: CameraDevice) {
                    }

                    override fun onError(p0: CameraDevice, p1: Int) {
                    }

                    override fun onOpened(p0: CameraDevice) {
                        val camDevice = p0

                        val cameraCharacteristics = manager.getCameraCharacteristics(cameraId)
                        val previewSize = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!.getOutputSizes(
                            ImageFormat.JPEG).maxByOrNull { it.height * it.width }!!
                        val imageReader = ImageReader.newInstance(previewSize.width, previewSize.height, ImageFormat.JPEG, 1)
                        imageReader.setOnImageAvailableListener(
                            { reader ->
                                val image: Image = reader.acquireLatestImage()
                            },
                            null
                        )

                        // Create capture
                        val captureBuilder = camDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
                        captureBuilder.addTarget(imageReader.surface)

                        // Create session
                        camDevice.createCaptureSession(
                            listOf(imageReader.surface),
                            object: CameraCaptureSession.StateCallback() {
                                override fun onConfigureFailed(p0: CameraCaptureSession) {
                                }

                                override fun onConfigured(p0: CameraCaptureSession) {
                                    val session = p0
                                    session.capture(captureBuilder.build(), object: CameraCaptureSession.CaptureCallback() {}, null)
                                }
                            },
                            null
                        )
                    }
                },
                null
            )
        }
    }

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
    ) {padding ->
    }
}
