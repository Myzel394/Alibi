package app.myzel394.locationtest.ui.components.SettingsScreen.atoms

import android.app.ProgressDialog.show
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
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
import com.maxkeppeler.sheets.list.ListDialog
import com.maxkeppeler.sheets.list.models.ListOption
import com.maxkeppeler.sheets.list.models.ListSelection
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutputFormatTile() {
    val scope = rememberCoroutineScope()
    val showDialog = rememberUseCaseState()
    val dataStore = LocalContext.current.dataStore
    val settings = dataStore
        .data
        .collectAsState(initial = AppSettings.getDefaultInstance())
        .value

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
        selection = ListSelection.Single(
            showRadioButtons = true,
            options = IntRange(0, 11).map { index ->
                ListOption(
                    titleText = AudioRecorderSettings.OUTPUT_FORMAT_INDEX_TEXT_MAP[index]!!,
                    selected = settings.audioRecorderSettings.outputFormat == index,
                )
            }.toList()
        ) {index, option ->
            updateValue(index)
        },
    )
    SettingsTile(
        title = "Output Format",
        description = "Define the output format of the audio file.",
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
                        "Auto"
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
                Text(
                    text = "Auto"
                )
            }
        }
    )
}