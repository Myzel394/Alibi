package app.myzel394.alibi.services

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import app.myzel394.alibi.MainActivity
import app.myzel394.alibi.NotificationHelper
import app.myzel394.alibi.R
import app.myzel394.alibi.db.NotificationSettings
import app.myzel394.alibi.enums.RecorderState
import kotlinx.serialization.Serializable
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar
import java.util.Date

data class RecorderNotificationHelper(
    val context: Context,
    val details: NotificationDetails? = null,
) {
    @Serializable
    data class NotificationDetails(
        val title: String,
        val description: String,
        val icon: Int,
        val isOngoing: Boolean,
    ) {
        companion object {
            fun fromNotificationSettings(
                context: Context,
                settings: NotificationSettings,
            ): NotificationDetails {
                return if (settings.preset == null) {
                    NotificationDetails(
                        settings.title,
                        settings.message,
                        settings.iconID,
                        settings.showOngoing,
                    )
                } else {
                    NotificationDetails(
                        context.getString(settings.preset.titleID),
                        context.getString(settings.preset.messageID),
                        settings.preset.iconID,
                        settings.preset.showOngoing,
                    )
                }
            }
        }
    }

    private fun getNotificationChangeStateIntent(
        newState: RecorderState,
        requestCode: Int
    ): PendingIntent {
        return PendingIntent.getService(
            context,
            requestCode,
            Intent(context, AudioRecorderService::class.java).apply {
                action = "changeState"
                putExtra("newState", newState.name)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun getIconID(): Int = details?.icon ?: R.drawable.launcher_monochrome_noopacity

    private fun createBaseNotification(): NotificationCompat.Builder {
        return NotificationCompat.Builder(
            context,
            NotificationHelper.RECORDER_CHANNEL_ID
        )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setSmallIcon(getIconID())
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    0,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            )
            .setSilent(true)
            .setOnlyAlertOnce(true)
            .setChronometerCountDown(false)
    }

    fun buildStartingNotification(): Notification {
        return createBaseNotification()
            .setContentTitle(context.getString(R.string.ui_audioRecorder_state_recording_title))
            .setContentText(context.getString(R.string.ui_audioRecorder_state_recording_description))
            .build()
    }

    fun buildRecordingNotification(recordingTime: Long): Notification {
        return createBaseNotification()
            .setUsesChronometer(details?.isOngoing ?: true)
            .setOngoing(details?.isOngoing ?: true)
            .setShowWhen(details?.isOngoing ?: true)
            .setWhen(
                Date.from(
                    Calendar
                        .getInstance()
                        .also { it.add(Calendar.MILLISECOND, -recordingTime.toInt()) }
                        .toInstant()
                ).time,
            )
            .addAction(
                R.drawable.ic_cancel,
                context.getString(R.string.ui_audioRecorder_action_delete_label),
                getNotificationChangeStateIntent(RecorderState.STOPPED, 1),
            )
            .addAction(
                R.drawable.ic_pause,
                context.getString(R.string.ui_audioRecorder_action_pause_label),
                getNotificationChangeStateIntent(RecorderState.PAUSED, 2),
            )
            .setContentTitle(
                details?.title
                    ?: context.getString(R.string.ui_audioRecorder_state_recording_title)
            )
            .setContentText(
                details?.description
                    ?: context.getString(R.string.ui_audioRecorder_state_recording_description)
            )
            .build()
    }

    fun buildPausedNotification(start: LocalDateTime): Notification {
        return createBaseNotification()
            .setContentTitle(context.getString(R.string.ui_audioRecorder_state_paused_title))
            .setContentText(context.getString(R.string.ui_audioRecorder_state_paused_description))
            .setOngoing(false)
            .setUsesChronometer(false)
            .setWhen(Date.from(start.atZone(ZoneId.systemDefault()).toInstant()).time)
            .addAction(
                R.drawable.ic_play,
                context.getString(R.string.ui_audioRecorder_action_resume_label),
                getNotificationChangeStateIntent(RecorderState.RECORDING, 3),
            )
            .build()
    }
}