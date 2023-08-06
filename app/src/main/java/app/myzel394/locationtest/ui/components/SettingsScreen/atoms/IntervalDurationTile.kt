package app.myzel394.locationtest.ui.components.SettingsScreen.atoms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.myzel394.locationtest.R
import app.myzel394.locationtest.dataStore
import app.myzel394.locationtest.db.AppSettings
import app.myzel394.locationtest.db.AudioRecorderSettings
import app.myzel394.locationtest.ui.components.atoms.ExampleListRoulette
import app.myzel394.locationtest.ui.components.atoms.SettingsTile
import app.myzel394.locationtest.ui.utils.formatDuration
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.duration.DurationDialog
import com.maxkeppeler.sheets.duration.models.DurationConfig
import com.maxkeppeler.sheets.duration.models.DurationFormat
import com.maxkeppeler.sheets.duration.models.DurationSelection
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntervalDurationTile() {
    val scope = rememberCoroutineScope()
    val showDialog = rememberUseCaseState()
    val dataStore = LocalContext.current.dataStore
    val settings = dataStore
        .data
        .collectAsState(initial = AppSettings.getDefaultInstance())
        .value

    fun updateValue(intervalDuration: Long) {
        scope.launch {
            dataStore.updateData {
                it.setAudioRecorderSettings(
                    it.audioRecorderSettings.setIntervalDuration(intervalDuration)
                )
            }
        }
    }

    DurationDialog(
        state = showDialog,
        selection = DurationSelection { newTimeInSeconds ->
            updateValue(newTimeInSeconds * 1000L)
        },
        config = DurationConfig(
            timeFormat = DurationFormat.MM_SS,
            currentTime = settings.audioRecorderSettings.intervalDuration / 1000,
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
                    text = formatDuration(settings.audioRecorderSettings.intervalDuration),
                )
            }
        },
        extra = {
            ExampleListRoulette(
                items = AudioRecorderSettings.EXAMPLE_DURATION_TIMES,
                onItemSelected = ::updateValue,
            ) {duration ->
                Text(
                    text = formatDuration(duration),
                )
            }
        }
    )
}