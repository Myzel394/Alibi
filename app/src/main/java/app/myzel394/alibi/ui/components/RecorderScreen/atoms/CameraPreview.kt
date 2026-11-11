package app.myzel394.alibi.ui.components.RecorderScreen.atoms

import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import app.myzel394.alibi.ui.utils.getCameraProvider
import kotlinx.coroutines.launch

@androidx.annotation.OptIn(androidx.camera.camera2.interop.ExperimentalCamera2Interop::class)
@Composable
fun CameraPreview(
    modifier: Modifier,
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    physicalCameraId: String? = null,
) {
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    Box(modifier = modifier) {
        // Video preview
        AndroidView(
            factory = { context ->
                val previewView = PreviewView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                }
                val previewUseCase = Preview.Builder()
                    .apply {
                        if (android.os.Build.VERSION.SDK_INT >= 28 && physicalCameraId != null)
                            androidx.camera.camera2.interop.Camera2Interop.Extender(this).setPhysicalCameraId(physicalCameraId)
                    }
                    .build()
                    .also { it.setSurfaceProvider(previewView.surfaceProvider) }

                coroutineScope.launch {
                    val cameraProvider = context.getCameraProvider()
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            previewUseCase
                        )
                    } catch (ex: Exception) {
                        Log.e("CameraPreview", "Use case binding failed", ex)
                    }
                }
                previewView
            },
        )
    }
}
