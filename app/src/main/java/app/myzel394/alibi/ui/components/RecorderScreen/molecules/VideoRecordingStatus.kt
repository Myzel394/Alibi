package app.myzel394.alibi.ui.components.RecorderScreen.molecules

import android.graphics.Color
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import app.myzel394.alibi.ui.models.VideoRecorderModel
import com.ujizin.camposer.CameraPreview

@Composable
fun VideoRecordingStatus(
    videoRecorder: VideoRecorderModel,
) {
    val lifecycle = LocalLifecycleOwner.current

    AndroidView(
        modifier = Modifier
            .fillMaxSize(),
        factory = {context ->
            PreviewView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    MATCH_PARENT,
                    MATCH_PARENT,
                )
                setBackgroundColor(Color.BLACK)
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }.also {previewView ->
                //videoRecorder.recorderService!!.cameraController.let {

            }
        }
    )
}
