package app.myzel394.alibi.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.helpers.SchedulerHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.i("BootCompletedReceiver", "Device boot completed. Rescheduling alarm if enabled.")
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                try {
                    val appSettings = context.dataStore.data.first()
                    if (appSettings.schedulerEnabled) {
                        SchedulerHelper.scheduleNextAlarm(context, appSettings)
                    } else {
                        Log.i("BootCompletedReceiver", "Scheduler is disabled. No alarm rescheduled.")
                    }
                } catch (e: Exception) {
                    Log.e("BootCompletedReceiver", "Error rescheduling alarm after boot: ${e.localizedMessage}", e)
                }
            }
        }
    }
}
