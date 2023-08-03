package app.myzel394.locationtest.ui.components.SettingsScreen.atoms

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
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
fun BitrateTile() {
    val scope = rememberCoroutineScope()
    val showDialog = rememberUseCaseState()
    val dataStore = LocalContext.current.dataStore
    val settings = dataStore
        .data
        .collectAsState(initial = AppSettings.getDefaultInstance())
        .value

    InputDialog(
        state = showDialog,
        selection = InputSelection(
            input = listOf(
                InputTextField(
                    header = InputHeader(
                        title = "Set the bitrate for the audio recording",
                        icon = IconSource(Icons.Default.Tune),
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                    ),
                    type = InputTextFieldType.OUTLINED,
                    text = (settings.audioRecorderSettings.bitRate / 1000).toString(),
                    validationListener = { text ->
                        val bitRate = text?.toIntOrNull()

                        if (bitRate == null) {
                            ValidationResult.Invalid("Please enter a valid number")
                        }

                        if (bitRate in 1..320) {
                            ValidationResult.Valid
                        } else {
                            ValidationResult.Invalid("Please enter a number between 1 and 320")
                        }
                    },
                    key = "bitrate",
                )
            ),
        ) { result ->
            val bitRate = result.getString("bitrate")?.toIntOrNull() ?: throw IllegalStateException("Bitrate is null")

            scope.launch {
                dataStore.updateData {
                    it.setAudioRecorderSettings(
                        it.audioRecorderSettings.setBitRate(bitRate * 1000)
                    )
                }
            }
        }
    )
    SettingsTile(
        title = "Bitrate",
        description = "A higher bitrate means better quality but also larger file size",
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
                    text = "${settings.audioRecorderSettings.bitRate / 1000} KB/s",
                )
            }
        },
        extra = {
            ExampleListRoulette(
                items = AudioRecorderSettings.EXAMPLE_BITRATE_VALUES,
                onItemSelected = { bitRate ->
                    scope.launch {
                        dataStore.updateData {
                            it.setAudioRecorderSettings(
                                it.audioRecorderSettings.setBitRate(bitRate)
                            )
                        }
                    }
                }
            ) {bitRate ->
                Text(
                    text = "${bitRate / 1000} KB/s",
                )
            }
        }
    )
}