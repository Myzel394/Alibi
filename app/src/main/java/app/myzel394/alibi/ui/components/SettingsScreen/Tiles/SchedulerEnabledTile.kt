package app.myzel394.alibi.ui.components.SettingsScreen.Tiles

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings as AndroidSettings
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import app.myzel394.alibi.R
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.helpers.SchedulerHelper
import app.myzel394.alibi.ui.components.atoms.Settings.ListTile
import app.myzel394.alibi.ui.components.atoms.Settings.ListTileSwitch
import kotlinx.coroutines.launch

@Composable
fun SchedulerEnabledTile(
    settings: AppSettings,
    icon: ImageVector = Icons.Outlined.Schedule,
    title: String = stringResource(R.string.ui_settings_schedulerEnabled_title),
    description: String? = stringResource(R.string.ui_settings_schedulerEnabled_description),
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val dataStore = context.dataStore
    var showPermissionDialog by remember { mutableStateOf(false) }

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun attemptEnableScheduler(enable: Boolean) {
        scope.launch {
            val newSettings = dataStore.updateData { currentSettings ->
                currentSettings.setSchedulerEnabled(enable)
            }
            if (enable) {
                SchedulerHelper.scheduleNextAlarm(context, newSettings)
            } else {
                SchedulerHelper.cancelAlarm(context) // Ensure alarm is cancelled if scheduler is disabled
            }
        }
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text(stringResource(R.string.ui_settings_scheduler_permission_dialog_title)) },
            text = { Text(stringResource(R.string.ui_settings_scheduler_permission_dialog_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val intent = Intent(AndroidSettings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                data = android.net.Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        }
                    }
                ) {
                    Text(stringResource(R.string.ui_settings_scheduler_permission_dialog_grant))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text(stringResource(R.string.dialog_close_cancel_label))
                }
            }
        )
    }

    ListTile(
        icon = icon,
        title = title,
        description = description,
    ) {
        ListTileSwitch(
            checked = settings.schedulerEnabled,
            onCheckedChange = { newEnabledState ->
                if (newEnabledState) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                        showPermissionDialog = true
                        // Do not toggle the switch, user needs to grant permission first
                    } else {
                        // Permission granted or SDK < S
                        attemptEnableScheduler(true)
                    }
                } else {
                    // Disabling the scheduler
                    attemptEnableScheduler(false)
                }
            },
        )
    }
}
