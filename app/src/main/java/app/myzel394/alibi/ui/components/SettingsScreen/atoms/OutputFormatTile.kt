package app.myzel394.alibi.ui.components.SettingsScreen.atoms

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
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
import com.maxkeppeker.sheets.core.models.base.Header
import com.maxkeppeker.sheets.core.models.base.IconSource
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.list.ListDialog
import com.maxkeppeler.sheets.list.models.ListOption
import com.maxkeppeler.sheets.list.models.ListSelection
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutputFormatTile(
    settings: AppSettings,
) {
    val scope = rememberCoroutineScope()
    val showDialog = rememberUseCaseState()
    val dataStore = LocalContext.current.dataStore
    val availableOptions = if (settings.audioRecorderSettings.encoder == null)
        AudioRecorderSettings.OUTPUT_FORMAT_INDEX_TEXT_MAP.keys.toTypedArray()
    else AudioRecorderSettings.ENCODER_SUPPORTED_OUTPUT_FORMATS_MAP[settings.audioRecorderSettings.encoder]!!

    fun updateValue(outputFormat: Int?) {
        scope.launch {
            dataStore.updateData {
                it.setAudioRecorderSettings(
                    it.audioRecorderSettings.setOutputFormat(outputFormat)
                )
            }
        }
    }

    ListDialog(
        state = showDialog,
        header = Header.Default(
            title = stringResource(R.string.ui_settings_option_outputFormat_title),
            icon = IconSource(
                painter = IconResource.fromImageVector(Icons.Default.AudioFile).asPainterResource(),
                contentDescription = null,
            )
        ),
        selection = ListSelection.Single(
            showRadioButtons = true,
            options = availableOptions.map { option ->
                ListOption(
                    titleText = AudioRecorderSettings.OUTPUT_FORMAT_INDEX_TEXT_MAP[option]!!,
                    selected = settings.audioRecorderSettings.outputFormat == option,
                )
            }.toList()
        ) { index, _ ->
            updateValue(availableOptions[index])
        },
    )
    SettingsTile(
        title = stringResource(R.string.ui_settings_option_outputFormat_title),
        leading = {
            Icon(
                Icons.Default.AudioFile,
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
                    text = if (settings.audioRecorderSettings.outputFormat == null) {
                        stringResource(R.string.ui_settings_value_auto_label)
                    } else {
                        AudioRecorderSettings.OUTPUT_FORMAT_INDEX_TEXT_MAP[settings.audioRecorderSettings.outputFormat]!!
                    }
                )
            }
        },
        extra = {
            ExampleListRoulette(
                items = listOf(null),
                onItemSelected = ::updateValue,
            ) {
                Text(stringResource(R.string.ui_settings_value_auto_label))
            }
        }
    )
}