package app.myzel394.alibi.receivers

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.core.content.ContextCompat
import app.myzel394.alibi.services.AudioRecorderService
import app.myzel394.alibi.services.RecorderService
import app.myzel394.alibi.ui.enums.Screen
import app.myzel394.alibi.ui.models.AudioRecorderModel

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        println("Received new intent: ${intent?.action}")

        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            println("Starting service")

            val connection = object : ServiceConnection {
                override fun onServiceConnected(className: ComponentName, service: IBinder) {
                    ((service as RecorderService.RecorderBinder).getService() as AudioRecorderService).also { recorder ->
                        recorder.startRecording()
                    }
                }

                override fun onServiceDisconnected(arg0: ComponentName) {
                }
            }

            val intent = Intent(context, AudioRecorderService::class.java).apply {
                action = "initStart"
            }
            println("Starting service checking context")
            if (context != null) {
                println("Starting service with context")
                ContextCompat.startForegroundService(context, intent)
            }
        }
    }
}