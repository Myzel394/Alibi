package app.myzel394.alibi.ui.components.SettingsScreen.Tiles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.myzel394.alibi.R
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.helpers.SchedulerHelper
import app.myzel394.alibi.ui.components.atoms.Settings.ListTile
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun SchedulerDaysTile(
    settings: AppSettings,
    icon: ImageVector = Icons.Outlined.DateRange,
    title: String = stringResource(R.string.ui_settings_schedulerDays_title),
    description: String? = stringResource(R.string.ui_settings_schedulerDays_description),
    enabled: Boolean = settings.schedulerEnabled
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val dataStore = context.dataStore
    val dayInitials = stringArrayResource(R.array.day_initials) // S, M, T, W, T, F, S

    // Map Calendar.DAY_OF_WEEK (1=Sunday to 7=Saturday) to 0-6 index for dayInitials
    val calendarToDayIndex = mapOf(
        Calendar.SUNDAY to 0,
        Calendar.MONDAY to 1,
        Calendar.TUESDAY to 2,
        Calendar.WEDNESDAY to 3,
        Calendar.THURSDAY to 4,
        Calendar.FRIDAY to 5,
        Calendar.SATURDAY to 6
    )
    val dayIndexToCalendar = calendarToDayIndex.entries.associateBy({ it.value }) { it.key }

    var selectedDays by remember(settings.schedulerDays) { mutableStateOf(settings.schedulerDays) }

    ListTile(
        icon = icon,
        title = title,
        description = description,
        enabled = enabled,
        onClick = if (!enabled) ({}) else null // Disable click on tile itself, interaction is via buttons
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            dayInitials.forEachIndexed { index, initial ->
                val calendarDay = dayIndexToCalendar[index] ?: throw IllegalStateException("Invalid day index")
                val isSelected = selectedDays.contains(calendarDay)

                FilledTonalButton(
                    onClick = {
                        if (enabled) {
                            val newSelectedDays = selectedDays.toMutableSet()
                            if (isSelected) {
                                newSelectedDays.remove(calendarDay)
                            } else {
                                newSelectedDays.add(calendarDay)
                            }
                            selectedDays = newSelectedDays
                            scope.launch {
                                val newSettings = dataStore.updateData { currentSettings ->
                                    currentSettings.setSchedulerDays(newSelectedDays)
                                }
                                SchedulerHelper.scheduleNextAlarm(context, newSettings)
                            }
                        }
                    },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isSelected) androidx.compose.material3.MaterialTheme.colorScheme.primary else androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (isSelected) androidx.compose.material3.MaterialTheme.colorScheme.onPrimary else androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.weight(1f).padding(horizontal = 2.dp),
                    enabled = enabled
                ) {
                    Text(initial)
                }
            }
        }
    }
}
