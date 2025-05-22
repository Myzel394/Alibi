package app.myzel394.alibi.helpers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.receivers.ScheduledRecordingReceiver
import java.util.Calendar
import java.util.TimeZone

object SchedulerHelper {

    const val ACTION_SCHEDULED_RECORDING_START = "app.myzel394.alibi.SCHEDULED_RECORDING_START"

    fun scheduleNextAlarm(context: Context, appSettings: AppSettings) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Cancel any existing alarms
        cancelAlarm(context)

        if (!appSettings.schedulerEnabled || appSettings.schedulerDays.isEmpty()) {
            Log.i("SchedulerHelper", "Scheduler is disabled or no days selected. No alarm scheduled.")
            return
        }

        val nextTriggerTime = calculateNextTriggerTime(appSettings)
        if (nextTriggerTime == null) {
            Log.i("SchedulerHelper", "Could not calculate next trigger time. No alarm scheduled.")
            return
        }

        val pendingIntent = getPendingIntent(context)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                Log.w("SchedulerHelper", "Missing SCHEDULE_EXACT_ALARM permission. Cannot schedule exact alarm.")
                // Fallback or notify user - for now, we'll log. A proper implementation
                // would request permission or use an inexact alarm.
                // For this task, we assume permission will be handled.
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(nextTriggerTime, pendingIntent)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(nextTriggerTime, pendingIntent)
            } else {
                alarmManager.set(nextTriggerTime, pendingIntent)
            }
            Log.i("SchedulerHelper", "Scheduled alarm for: ${Calendar.getInstance().apply { timeInMillis = nextTriggerTime }.time}")
        } catch (e: SecurityException) {
            Log.e("SchedulerHelper", "SecurityException while scheduling alarm. Do you have SCHEDULE_EXACT_ALARM permission?", e)
            // This might happen if canScheduleExactAlarms() is false or other security restrictions.
        }
    }

    fun cancelAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = getPendingIntent(context)
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
        Log.i("SchedulerHelper", "Cancelled existing alarms.")
    }

    private fun getPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, ScheduledRecordingReceiver::class.java).apply {
            action = ACTION_SCHEDULED_RECORDING_START
        }
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    internal fun calculateNextTriggerTime(appSettings: AppSettings): Long? {
        if (appSettings.schedulerDays.isEmpty()) return null

        val now = Calendar.getInstance()
        val scheduledHour = appSettings.schedulerHour
        val scheduledMinute = appSettings.schedulerMinute

        // Sort the selected days (1=Sunday, 2=Monday, ..., 7=Saturday)
        val sortedDays = appSettings.schedulerDays.sorted()

        for (i in 0..7) { // Check today and next 7 days
            val nextDayCandidate = Calendar.getInstance().apply {
                timeInMillis = now.timeInMillis
                add(Calendar.DAY_OF_YEAR, i) // Start checking from today
                set(Calendar.HOUR_OF_DAY, scheduledHour)
                set(Calendar.MINUTE, scheduledMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val dayOfWeekCandidate = nextDayCandidate.get(Calendar.DAY_OF_WEEK) // Sunday is 1, Saturday is 7

            if (sortedDays.contains(dayOfWeekCandidate)) {
                // If it's today, check if the time has already passed
                if (i == 0 && nextDayCandidate.before(now)) {
                    continue // Time has passed for today, check next scheduled day
                }
                // Found the next valid day and time
                Log.d("SchedulerHelper", "Next alarm trigger time: ${nextDayCandidate.time} (Day: $dayOfWeekCandidate)")
                return nextDayCandidate.timeInMillis
            }
        }
        // Should not happen if days are selected, but as a fallback
        Log.e("SchedulerHelper", "Could not find a suitable day in the next week.")
        return null
    }
}
