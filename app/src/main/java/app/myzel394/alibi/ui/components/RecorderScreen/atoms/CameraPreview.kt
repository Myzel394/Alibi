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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import app.myzel394.alibi.ui.utils.getCameraProvider
import kotlinx.coroutines.launch

@Composable
fun CameraPreview(
    modifier: Modifier,
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
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
