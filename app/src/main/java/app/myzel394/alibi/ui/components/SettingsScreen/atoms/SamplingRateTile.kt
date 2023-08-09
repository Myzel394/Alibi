package app.myzel394.alibi.ui.components.SettingsScreen.atoms

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RadioButtonChecked
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import app.myzel394.alibi.R
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.db.AudioRecorderSettings
import app.myzel394.alibi.ui.components.atoms.ExampleListRoulette
import app.myzel394.alibi.ui.components.atoms.SettingsTile
import app.myzel394.alibi.ui.utils.IconResource
import com.maxkeppeker.sheets.core.models.base.Header
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

    val notNumberLabel = stringResource(R.string.form_error_type_notNumber)
    val mustBeGreaterThanLabel = stringResource(R.string.form_error_value_mustBeGreaterThan, 1000)
    InputDialog(
        state = showDialog,
        header = Header.Default(
            title = stringResource(R.string.ui_settings_option_samplingRate_title),
            icon = IconSource(
                painter = IconResource.fromImageVector(Icons.Default.RadioButtonChecked).asPainterResource(),
                contentDescription = null,
            )
        ),
        selection = InputSelection(
            input = listOf(
                InputTextField(
                    header = InputHeader(
                        title = stringResource(R.string.ui_settings_option_samplingRate_explanation),
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                    ),
                    type = InputTextFieldType.OUTLINED,
                    text = settings.audioRecorderSettings.getSamplingRate().toString(),
                    validationListener = { text ->
                        val samplingRate = text?.toIntOrNull()

                        if (samplingRate == null) {
                            ValidationResult.Invalid(notNumberLabel)
                        }

                        if (samplingRate!! <= 1000) {
                            ValidationResult.Invalid(mustBeGreaterThanLabel)
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
        title = stringResource(R.string.ui_settings_option_samplingRate_title),
        description = stringResource(R.string.ui_settings_option_samplingRate_description),
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
                    (settings.audioRecorderSettings.samplingRate ?: stringResource(R.string.ui_settings_value_auto_label)).toString()
                )
            }
        },
        extra = {
            ExampleListRoulette(
                items = AudioRecorderSettings.EXAMPLE_SAMPLING_RATE,
                onItemSelected = ::updateValue,
            ) {samplingRate ->
                Text(
                    (samplingRate ?: stringResource(R.string.ui_settings_value_auto_label)).toString()
                )
            }
        }
    )
}