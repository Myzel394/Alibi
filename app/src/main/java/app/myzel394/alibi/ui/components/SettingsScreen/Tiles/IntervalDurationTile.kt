package app.myzel394.alibi.ui.components.SettingsScreen.Tiles

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import app.myzel394.alibi.R
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.db.AudioRecorderSettings
import app.myzel394.alibi.ui.components.atoms.ExampleListRoulette
import app.myzel394.alibi.ui.components.atoms.SettingsTile
import app.myzel394.alibi.ui.utils.IconResource
import app.myzel394.alibi.ui.utils.formatDuration
import com.maxkeppeker.sheets.core.models.base.Header
import com.maxkeppeker.sheets.core.models.base.IconSource
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.duration.DurationDialog
import com.maxkeppeler.sheets.duration.models.DurationConfig
import com.maxkeppeler.sheets.duration.models.DurationFormat
import com.maxkeppeler.sheets.duration.models.DurationSelection
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntervalDurationTile(
    settings: AppSettings,
) {
    val scope = rememberCoroutineScope()
    val showDialog = rememberUseCaseState()
    val dataStore = LocalContext.current.dataStore

    fun updateValue(intervalDuration: Long) {
        scope.launch {
            if (intervalDuration > settings.maxDuration) {
                dataStore.updateData {
                    it.setMaxDuration(intervalDuration)
                }
            }

            dataStore.updateData {
                it.setIntervalDuration(intervalDuration)
            }
        }
    }

    DurationDialog(
        state = showDialog,
        header = Header.Default(
            title = stringResource(R.string.ui_settings_option_intervalDuration_title),
            icon = IconSource(
                painter = IconResource.fromImageVector(Icons.Default.Mic).asPainterResource(),
                contentDescription = null,
            )
        ),
        selection = DurationSelection { newTimeInSeconds ->
            updateValue(newTimeInSeconds * 1000L)
        },
        config = DurationConfig(
            timeFormat = DurationFormat.MM_SS,
            currentTime = settings.intervalDuration / 1000,
            minTime = 10,
            maxTime = 60 * 60,
        )
    )
    SettingsTile(
        title = stringResource(R.string.ui_settings_option_intervalDuration_title),
        description = stringResource(R.string.ui_settings_option_intervalDuration_description),
        leading = {
            Icon(
                Icons.Default.Mic,
                contentDescription = null,
            )
        },
        trailing = {
            Button(
                onClick = showDialog::show,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(
                    text = formatDuration(settings.intervalDuration),
                )
            }
        },
        extra = {
            ExampleListRoulette(
                items = AudioRecorderSettings.EXAMPLE_DURATION_TIMES,
                onItemSelected = ::updateValue,
            ) { duration ->
                Text(
                    text = formatDuration(duration),
                )
            }
        }
    )
}