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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import app.myzel394.alibi.services.AudioRecorderService
import app.myzel394.alibi.services.RecorderService
import app.myzel394.alibi.services.VideoService

@Composable
fun POCVideo() {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(64.dp)
    ) {
        Button(onClick = {
            val connection = object : android.content.ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    val binder =
                        ((service as RecorderService.RecorderBinder).getService() as VideoService).also { recorder ->
                            recorder.settings = VideoService.Settings.from()
                            recorder.startRecording()
                        }
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                }
            }

            val intent = Intent(context, VideoService::class.java).apply {
                action = "init"
            }
            ContextCompat.startForegroundService(context, intent)
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }) {
            Text("Start")
        }
    }
}