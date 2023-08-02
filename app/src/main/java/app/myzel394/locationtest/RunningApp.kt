package app.myzel394.locationtest

import android.app.Application
import android.app.NotificationChannel
import android.os.Build

class RunningApp: Application() {
    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "recorder",
                "Recorder",
                android.app.NotificationManager.IMPORTANCE_LOW,
            )
            val notificationManager = getSystemService(android.app.NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}