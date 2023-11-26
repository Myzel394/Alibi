package app.myzel394.alibi.ui.screens

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.services.AudioRecorderService
import app.myzel394.alibi.services.RecorderService
import app.myzel394.alibi.services.VideoService
import app.myzel394.alibi.ui.models.VideoRecorderModel

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
            }

            started = !started
        }) {
            Text("Start")
        }
    }
}