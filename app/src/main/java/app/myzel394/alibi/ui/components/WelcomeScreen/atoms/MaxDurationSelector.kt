package app.myzel394.alibi.ui.components.WelcomeScreen.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.myzel394.alibi.R
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.ui.utils.IconResource
import com.maxkeppeker.sheets.core.models.base.Header
import com.maxkeppeker.sheets.core.models.base.IconSource
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.duration.DurationDialog
import com.maxkeppeler.sheets.duration.models.DurationConfig
import com.maxkeppeler.sheets.duration.models.DurationFormat
import com.maxkeppeler.sheets.duration.models.DurationSelection
import kotlinx.coroutines.launch

const val MINUTES_1 = 1000 * 60 * 1L
const val MINUTES_5 = 1000 * 60 * 5L
const val MINUTES_15 = 1000 * 60 * 15L
const val MINUTES_30 = 1000 * 60 * 30L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaxDurationSelector(
    modifier: Modifier = Modifier,
) {
    val OPTIONS = mapOf<Long, String>(
        MINUTES_1 to stringResource(R.string.ui_welcome_timeSettings_values_1min),
        MINUTES_5 to stringResource(R.string.ui_welcome_timeSettings_values_5min),
        MINUTES_15 to stringResource(R.string.ui_welcome_timeSettings_values_15min),
        MINUTES_30 to stringResource(R.string.ui_welcome_timeSettings_values_30min),
    )

    val scope = rememberCoroutineScope()
    val dataStore = LocalContext.current.dataStore

    var selectedDuration by rememberSaveable { mutableLongStateOf(MINUTES_15) };

    // Make sure appSettings is updated properly
    LaunchedEffect(selectedDuration) {
        scope.launch {
            dataStore.updateData {
                it.setMaxDuration(selectedDuration)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .then(modifier),
        verticalArrangement = Arrangement.Center,
    ) {
        for ((duration, label) in OPTIONS) {
            val a11yLabel = stringResource(
                R.string.a11y_selectValue,
                label
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .semantics {
                        contentDescription = a11yLabel
                    }
                    .clickable {
                        selectedDuration = duration
                    }
                    .padding(16.dp)
            ) {
                RadioButton(
                    selected = selectedDuration == duration,
                    onClick = { selectedDuration = duration },
                )
                Text(label)
            }
        }

        let {
            val showDialog = rememberUseCaseState()
            val label = stringResource(R.string.ui_welcome_timeSettings_values_custom)
            val selected = selectedDuration !in OPTIONS.keys

            DurationDialog(
                state = showDialog,
                header = Header.Default(
                    title = stringResource(R.string.ui_settings_option_maxDuration_title),
                    icon = IconSource(
                        painter = IconResource.fromImageVector(Icons.Default.Timer)
                            .asPainterResource(),
                        contentDescription = null,
                    )
                ),
                selection = DurationSelection { newTimeInSeconds ->
                    selectedDuration = newTimeInSeconds * 1000L
                },
                config = DurationConfig(
                    timeFormat = DurationFormat.HH_MM,
                    currentTime = selectedDuration / 1000,
                    minTime = 60,
                    maxTime = 23 * 60 * 60 + 60 * 59,
                )
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = label
                    }
                    .clickable {
                        showDialog.show()
                    }
                    .clip(MaterialTheme.shapes.medium)
                    .padding(16.dp)
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .padding(2.dp),
                    tint = if (selected) MaterialTheme.colorScheme.primary else contentColorFor(
                        MaterialTheme.colorScheme.surfaceContainer
                    )
                )
                if (selected) {
                    val totalMinutes = selectedDuration / 1000 / 60
                    val minutes = totalMinutes % 60
                    val hours = (totalMinutes / 60).toInt()

                    Text(
                        text = when (hours) {
                            0 -> stringResource(
                                R.string.ui_welcome_timeSettings_values_customFormat_mm,
                                minutes
                            )

                            1 -> stringResource(
                                R.string.ui_welcome_timeSettings_values_customFormat_h_mm,
                                minutes
                            )

                            else -> stringResource(
                                R.string.ui_welcome_timeSettings_values_customFormat_hh_mm,
                                hours,
                                minutes
                            )
                        },
                        color = MaterialTheme.colorScheme.primary,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.ui_welcome_timeSettings_values_custom),
                    )
                }
            }
        }
    }
}