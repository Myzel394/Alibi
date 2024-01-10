package app.myzel394.alibi.ui.components.SettingsScreen.Tiles

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
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
fun AudioRecorderBitrateTile(
    settings: AppSettings,
) {
    val scope = rememberCoroutineScope()
    val showDialog = rememberUseCaseState()
    val dataStore = LocalContext.current.dataStore

    fun updateValue(bitRate: Int) {
        scope.launch {
            dataStore.updateData {
                it.setAudioRecorderSettings(
                    it.audioRecorderSettings.setBitRate(bitRate)
                )
            }
        }
    }

    val notNumberLabel = stringResource(R.string.form_error_type_notNumber)
    val notInRangeLabel = stringResource(R.string.form_error_value_notInRange, 1, 320)
    InputDialog(
        state = showDialog,
        header = Header.Default(
            title = stringResource(R.string.ui_settings_option_bitrate_title),
            icon = IconSource(
                painter = IconResource.fromImageVector(Icons.Default.Tune).asPainterResource(),
                contentDescription = null,
            )
        ),
        selection = InputSelection(
            input = listOf(
                InputTextField(
                    header = InputHeader(
                        title = stringResource(id = R.string.ui_settings_option_bitrate_explanation),
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                    ),
                    type = InputTextFieldType.OUTLINED,
                    text = (settings.audioRecorderSettings.bitRate / 1000).toString(),
                    validationListener = { text ->
                        val bitRate = text?.toIntOrNull()

                        if (bitRate == null) {
                            return@InputTextField ValidationResult.Invalid(notNumberLabel)
                        }

                        if (bitRate !in 1..320) {
                            return@InputTextField ValidationResult.Invalid(notInRangeLabel)
                        }

                        ValidationResult.Valid
                    },
                    key = "bitrate",
                )
            ),
        ) { result ->
            val bitRate = result.getString("bitrate")?.toIntOrNull() ?: throw IllegalStateException(
                "Bitrate is null"
            )

            updateValue(bitRate * 1000)
        }
    )
    SettingsTile(
        title = stringResource(R.string.ui_settings_option_bitrate_title),
        description = stringResource(R.string.ui_settings_option_bitrate_description),
        leading = {
            Icon(
                Icons.Default.Tune,
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
                    stringResource(
                        R.string.format_kbps,
                        settings.audioRecorderSettings.bitRate / 1000,
                    ),
                )
            }
        },
        extra = {
            ExampleListRoulette(
                items = AudioRecorderSettings.EXAMPLE_BITRATE_VALUES,
                onItemSelected = ::updateValue,
            ) { bitRate ->
                Text(
                    stringResource(
                        R.string.format_kbps,
                        bitRate / 1000,
                    ),
                )
            }
        }
    )
}