package app.myzel394.alibi

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi

object NotificationHelper {
    @RequiresApi(Build.VERSION_CODES.O)
    fun createChannels(context: Context) {
        val channel = NotificationChannel(
            "recorder",
            context.resources.getString(R.string.notificationChannels_recorder_name),
            android.app.NotificationManager.IMPORTANCE_LOW,
        )
        channel.description = context.resources.getString(R.string.notificationChannels_recorder_description)

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
}