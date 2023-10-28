package app.myzel394.alibi.receivers

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import app.myzel394.alibi.MainActivity
import app.myzel394.alibi.NotificationHelper
import app.myzel394.alibi.R
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.helpers.AudioRecorderExporter
import app.myzel394.alibi.services.AudioRecorderService
import app.myzel394.alibi.services.RecorderNotificationHelper
import app.myzel394.alibi.services.RecorderService
import app.myzel394.alibi.ui.enums.Screen
import app.myzel394.alibi.ui.models.AudioRecorderModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class BootReceiver : BroadcastReceiver() {
    private var job = SupervisorJob()
    private var scope = CoroutineScope(Dispatchers.IO + job)

    private fun startRecording(context: Context, settings: AppSettings) {
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
            action = "init"
            if (settings.notificationSettings != null) {
                putExtra(
                    "notificationDetails",
                    Json.encodeToString(
                        RecorderNotificationHelper.NotificationDetails.serializer(),
                        RecorderNotificationHelper.NotificationDetails.fromNotificationSettings(
                            context,
                            settings.notificationSettings
                        )
                    ),
                )
            }
        }
        ContextCompat.startForegroundService(context, intent)
        context.bindService(intent, connection, 0)
    }

    private fun showNotification(context: Context) {
        if (!AudioRecorderExporter.hasRecordingsAvailable(context)) {
            // Nothing interrupted, so no notification needs to be shown
            return
        }

        val notification = NotificationCompat.Builder(context, NotificationHelper.BOOT_CHANNEL_ID)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setSmallIcon(R.drawable.launcher_monochrome_noopacity)
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    0,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            )
            .setOnlyAlertOnce(true)
            .setContentTitle(context.getString(R.string.notification_boot_title))
            .setContentText(context.getString(R.string.notification_boot_message))
            .build()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(NotificationHelper.BOOT_CHANNEL_NOTIFICATION_ID, notification)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED || context == null) {
            return
        }

        scope.launch {
            context.dataStore.data.collectLatest { settings ->
                when (settings.bootBehavior) {
                    AppSettings.BootBehavior.CONTINUE_RECORDING -> {
                        if (AudioRecorderExporter.hasRecordingsAvailable(context)) {
                            startRecording(context, settings)
                        }
                    }

                    AppSettings.BootBehavior.START_RECORDING -> startRecording(context, settings)
                    AppSettings.BootBehavior.SHOW_NOTIFICATION -> showNotification(context)
                    null -> {
                        // Nothing to do
                    }
                }
            }
        }
    }
}