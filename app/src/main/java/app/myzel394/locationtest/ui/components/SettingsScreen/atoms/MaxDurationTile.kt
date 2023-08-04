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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import app.myzel394.locationtest.dataStore
import app.myzel394.locationtest.db.AppSettings
import app.myzel394.locationtest.db.AudioRecorderSettings
import app.myzel394.locationtest.ui.components.atoms.ExampleListRoulette
import app.myzel394.locationtest.ui.components.atoms.SettingsTile
import app.myzel394.locationtest.ui.utils.formatDuration
import com.maxkeppeker.sheets.core.models.base.IconSource
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
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

    InputDialog(
        state = showDialog,
        selection = InputSelection(
            input = listOf(
                InputTextField(
                    header = InputHeader(
                        title = "Set the maximum duration",
                        icon = IconSource(Icons.Default.Timer),
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                    ),
                    type = InputTextFieldType.OUTLINED,
                    text = settings.audioRecorderSettings.maxDuration.toString(),
                    validationListener = { text ->
                        val maxDuration = text?.toLongOrNull()

                        if (maxDuration == null) {
                            ValidationResult.Invalid("Please enter a valid number")
                        }

                        if (maxDuration !in (60 * 1000L)..(60 * 60 * 1000L)) {
                            ValidationResult.Invalid("Please enter a number between 1 minute and 1 hour")
                        }

                        ValidationResult.Valid
                    },
                    key = "maxDuration",
                )
            ),
        ) { result ->
            val maxDuration = result.getString("maxDuration")?.toLongOrNull() ?: throw IllegalStateException("Invalid maxDuration")

            updateValue(maxDuration)
        }
    )
    SettingsTile(
        title = "Max duration",
        description = "Set the maximum duration of the recording",
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