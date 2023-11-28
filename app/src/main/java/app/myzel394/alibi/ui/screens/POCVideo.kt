package app.myzel394.alibi.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.ui.models.VideoRecorderModel

@SuppressLint("NewApi")
@Composable
fun POCVideo(
    videoRecorder: VideoRecorderModel,
    settings: AppSettings,
) {
    val context = LocalContext.current

    var started by rememberSaveable {
        mutableStateOf(false)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(64.dp)
    ) {
        Button(onClick = {
            if (!started) {
                videoRecorder.startRecording(context, settings)
            } else {
                videoRecorder.stopRecording(context)

                val folder = "content://media/external/video/media/DCIM/Recordings"
            }

            started = !started
        }) {
            Text("Start")
        }
    }
}