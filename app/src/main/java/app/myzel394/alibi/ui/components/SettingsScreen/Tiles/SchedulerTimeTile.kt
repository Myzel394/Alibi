package app.myzel394.alibi.ui.components.SettingsScreen.Tiles

import android.app.TimePickerDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import app.myzel394.alibi.R
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.helpers.SchedulerHelper
import app.myzel394.alibi.ui.components.atoms.Settings.ListTile
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun SchedulerTimeTile(
    settings: AppSettings,
    icon: ImageVector = Icons.Outlined.EditCalendar,
    title: String = stringResource(R.string.ui_settings_schedulerTime_title),
    description: String? = stringResource(
        R.string.ui_settings_schedulerTime_description,
        String.format(Locale.getDefault(), "%02d:%02d", settings.schedulerHour, settings.schedulerMinute)
    ),
    enabled: Boolean = settings.schedulerEnabled
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val dataStore = context.dataStore

    ListTile(
        icon = icon,
        title = title,
        description = description,
        enabled = enabled,
        onClick = {
            if (enabled) {
                val timePickerDialog = TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        scope.launch {
                            val newSettings = dataStore.updateData { currentSettings ->
                                currentSettings.setSchedulerTime(hourOfDay, minute)
                            }
                            SchedulerHelper.scheduleNextAlarm(context, newSettings)
                        }
                    },
                    settings.schedulerHour,
                    settings.schedulerMinute,
                    true // 24-hour format
                )
                timePickerDialog.show()
            }
        }
    )
}
