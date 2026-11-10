package app.myzel394.alibi.ui.utils

import android.content.Context
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException

suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCancellableCoroutine { continuation ->
    ProcessCameraProvider.getInstance(this).also { future ->
        future.addListener({
            if (continuation.isActive) {
                try { continuation.resume(future.get()) }
                catch (error: Exception) { continuation.resumeWithException(error) }
            }
        }, ContextCompat.getMainExecutor(this))
    }
}

