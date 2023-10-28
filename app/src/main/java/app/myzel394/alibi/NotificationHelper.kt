package app.myzel394.alibi

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi

object NotificationHelper {
    const val RECORDER_CHANNEL_ID = "recorder"
    const val RECORDER_CHANNEL_NOTIFICATION_ID = 1
    const val BOOT_CHANNEL_ID = "boot"
    const val BOOT_CHANNEL_NOTIFICATION_ID = 2

    @RequiresApi(Build.VERSION_CODES.O)
    fun createChannels(context: Context) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(
            NotificationChannel(
                RECORDER_CHANNEL_ID,
                context.getString(R.string.notificationChannels_recorder_name),
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = context.getString(R.string.notificationChannels_recorder_description)
            }
        )
        notificationManager.createNotificationChannel(
            NotificationChannel(
                BOOT_CHANNEL_ID,
                context.getString(R.string.notificationChannels_boot_name),
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = context.getString(R.string.notificationChannels_boot_description)
            }
        )
    }

}