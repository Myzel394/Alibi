package app.myzel394.alibi.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import app.myzel394.alibi.NotificationHelper

class LocalChangeReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, p1: Intent?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationHelper.createChannels(context!!)
        }
    }
}