package app.myzel394.locationtest

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaPlayer
import android.os.IBinder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat

@Composable
fun AudioRecorder() {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
    }

    var service by remember { mutableStateOf<RecorderService?>(null) }
    val connection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                service = (binder as RecorderService.LocalBinder).getService().also {service ->
                    service.setOnStateChangeListener {
                        println("asd")
                    }
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
            }
        }
    }

    val isRecording = service?.isRecording ?: false

    LaunchedEffect(Unit) {
        Intent(context, RecorderService::class.java).also {intent ->
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    Row {
        Button(
            onClick = {
                // Check audio recording permission
                if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    launcher.launch(Manifest.permission.RECORD_AUDIO)

                    return@Button
                }

                if (isRecording) {
                    Intent(context, RecorderService::class.java).also {intent ->
                        intent.action = RecorderService.Actions.STOP.toString()

                        context.startService(intent)
                    }
                } else {
                    Intent(context, RecorderService::class.java).also {intent ->
                        intent.action = RecorderService.Actions.START.toString()

                        ContextCompat.startForegroundService(context, intent)
                        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
                    }
                }
            },
        ) {
            Text(text = if (isRecording) "Stop" else "Start")
        }
        if (!isRecording && service != null)
            Button(
                onClick = {
                  val path = service!!.concatenateAudios()

                    val player = MediaPlayer().apply {
                        setDataSource(path)
                        prepare()
                    }

                    player.start()
                },
            ) {
                Text(text = "Convert")
            }
    }
}