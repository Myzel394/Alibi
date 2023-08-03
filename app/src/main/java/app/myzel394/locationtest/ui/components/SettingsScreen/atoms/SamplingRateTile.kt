package app.myzel394.locationtest.ui.components.SettingsScreen.atoms

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RadioButtonChecked
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
import androidx.compose.ui.text.input.KeyboardType
import app.myzel394.locationtest.dataStore
import app.myzel394.locationtest.db.AppSettings
import app.myzel394.locationtest.db.AudioRecorderSettings
import app.myzel394.locationtest.ui.components.atoms.ExampleListRoulette
import app.myzel394.locationtest.ui.components.atoms.SettingsTile
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
fun SamplingRateTile() {
    val scope = rememberCoroutineScope()
    val showDialog = rememberUseCaseState()
    val dataStore = LocalContext.current.dataStore
    val settings = dataStore
        .data
        .collectAsState(initial = AppSettings.getDefaultInstance())
        .value

    fun updateValue(samplingRate: Int?) {
        scope.launch {
            dataStore.updateData {
                it.setAudioRecorderSettings(
                    it.audioRecorderSettings.setSamplingRate(samplingRate)
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
                        title = "Set the sampling rate",
                        icon = IconSource(Icons.Default.RadioButtonChecked),
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                    ),
                    type = InputTextFieldType.OUTLINED,
                    text = settings.audioRecorderSettings.getSamplingRate().toString(),
                    validationListener = { text ->
                        val samplingRate = text?.toIntOrNull()

                        if (samplingRate == null) {
                            ValidationResult.Invalid("Please enter a valid number")
                        }

                        if (samplingRate!! <= 1000) {
                            ValidationResult.Invalid("Sampling rate must be greater than 1000")
                        }

                        ValidationResult.Valid
                    },
                    key = "samplingRate",
                )
            ),
        ) { result ->
            val samplingRate = result.getString("samplingRate")?.toIntOrNull() ?: throw IllegalStateException("SamplingRate is null")

            updateValue(samplingRate)
        }
    )
    SettingsTile(
        title = "Sampling rate",
        description = "Define how many samples per second are taken from the audio signal",
        leading = {
            Icon(
                Icons.Default.RadioButtonChecked,
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
                    text = (settings.audioRecorderSettings.samplingRate ?: "Auto").toString()
                )
            }
        },
        extra = {
            ExampleListRoulette(
                items = AudioRecorderSettings.EXAMPLE_SAMPLING_RATE,
                onItemSelected = ::updateValue,
            ) {samplingRate ->
                Text(
                    text = (samplingRate ?: "Auto").toString()
                )
            }
        }
    )
}