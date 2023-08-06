package app.myzel394.locationtest.ui.components.SettingsScreen.atoms

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import app.myzel394.locationtest.R
import app.myzel394.locationtest.dataStore
import app.myzel394.locationtest.db.AppSettings
import app.myzel394.locationtest.db.AudioRecorderSettings
import app.myzel394.locationtest.ui.components.atoms.ExampleListRoulette
import app.myzel394.locationtest.ui.components.atoms.SettingsTile
import app.myzel394.locationtest.ui.utils.formatDuration
import com.maxkeppeker.sheets.core.models.base.IconSource
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.duration.DurationDialog
import com.maxkeppeler.sheets.duration.models.DurationConfig
import com.maxkeppeler.sheets.duration.models.DurationFormat
import com.maxkeppeler.sheets.duration.models.DurationSelection
import com.maxkeppeler.sheets.input.InputDialog
import com.maxkeppeler.sheets.input.models.InputHeader
import com.maxkeppeler.sheets.input.models.InputSelection
import com.maxkeppeler.sheets.input.models.InputTextField
import com.maxkeppeler.sheets.input.models.InputTextFieldType
import com.maxkeppeler.sheets.input.models.ValidationResult
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
                Text(
                    text = formatDuration(settings.audioRecorderSettings.maxDuration),
                )
            }
        },
        extra = {
            ExampleListRoulette(
                items = AudioRecorderSettings.EXAMPLE_MAX_DURATIONS,
                onItemSelected = ::updateValue,
            ) {maxDuration ->
                Text(
                    text = formatDuration(maxDuration),
                )
            }
        }
    )
}