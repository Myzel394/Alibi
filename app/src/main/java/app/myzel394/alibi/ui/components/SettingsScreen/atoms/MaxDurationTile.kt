package app.myzel394.alibi.ui.components.SettingsScreen.atoms

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import app.myzel394.alibi.R
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.db.AudioRecorderSettings
import app.myzel394.alibi.ui.components.atoms.ExampleListRoulette
import app.myzel394.alibi.ui.components.atoms.SettingsTile
import app.myzel394.alibi.ui.utils.formatDuration
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.duration.DurationDialog
import com.maxkeppeler.sheets.duration.models.DurationConfig
import com.maxkeppeler.sheets.duration.models.DurationFormat
import com.maxkeppeler.sheets.duration.models.DurationSelection
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaxDurationTile() {
    val scope = rememberCoroutineScope()
    val showDialog = rememberUseCaseState()
    val dataStore = LocalContext.current.dataStore
    val settings = dataStore
        .data
        .collectAsState(initial = AppSettings.getDefaultInstance())
        .value

    fun updateValue(maxDuration: Long) {
        scope.launch {
            dataStore.updateData {
                it.setAudioRecorderSettings(
                    it.audioRecorderSettings.setMaxDuration(maxDuration)
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
            currentTime = settings.audioRecorderSettings.maxDuration / 1000,
            minTime = 60,
            maxTime = 24 * 60 * 60,
        )
    )
    SettingsTile(
        title = stringResource(R.string.ui_settings_option_maxDuration_title),
        description = stringResource(R.string.ui_settings_option_maxDuration_description),
        leading = {
            Icon(
                Icons.Default.Timer,
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
                Text(formatDuration(settings.audioRecorderSettings.maxDuration))
            }
        },
        extra = {
            ExampleListRoulette(
                items = AudioRecorderSettings.EXAMPLE_MAX_DURATIONS,
                onItemSelected = ::updateValue,
            ) {maxDuration ->
                Text(formatDuration(maxDuration))
            }
        }
    )
}