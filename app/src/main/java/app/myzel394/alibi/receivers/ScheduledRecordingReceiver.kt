package app.myzel394.alibi.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.helpers.SchedulerHelper
import app.myzel394.alibi.services.AudioRecorderService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ScheduledRecordingReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("ScheduledRecordingReceiver", "Received intent with action: ${intent.action}")
        if (intent.action == SchedulerHelper.ACTION_SCHEDULED_RECORDING_START) {
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                try {
                    val appSettings = context.dataStore.data.first()

                    if (appSettings.schedulerEnabled) {
                        if (AudioRecorderService.isServiceActive) {
                            Log.i("ScheduledRecordingReceiver", "Audio recording service is already active. Skipping scheduled start.")
                        } else {
                            Log.i("ScheduledRecordingReceiver", "Starting scheduled audio recording.")
                            // For now, hardcoding to start AudioRecorderService.
                            // This can be made configurable later.
                            val serviceIntent = Intent(context, AudioRecorderService::class.java).apply {
                                // Pass any necessary extras to the service, if needed.
                                // For example, indicating it's a scheduled start.
                                putExtra("isScheduledStart", true)
                                action = "init" // Assuming 'init' is the action to start recording.
                                               // This needs to match AudioRecorderService's expected intent action.
                            }
                            // Using startForegroundService for Android O+ compatibility
                            context.startForegroundService(serviceIntent)
                        }
                        // Always reschedule for the next occurrence, even if current one was skipped
                        SchedulerHelper.scheduleNextAlarm(context, appSettings)
                    } else {
                        Log.i("ScheduledRecordingReceiver", "Scheduler is disabled in settings. Not starting or rescheduling.")
                    }
                } catch (e: Exception) {
                    Log.e("ScheduledRecordingReceiver", "Error processing scheduled recording: ${e.localizedMessage}", e)
                }
            }
        }
    }
}
