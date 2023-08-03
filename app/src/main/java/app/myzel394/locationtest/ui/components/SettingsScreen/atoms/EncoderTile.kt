package app.myzel394.locationtest.ui.components.SettingsScreen.atoms

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Memory
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
import app.myzel394.locationtest.dataStore
import app.myzel394.locationtest.db.AppSettings
import app.myzel394.locationtest.db.AudioRecorderSettings
import app.myzel394.locationtest.ui.components.atoms.ExampleListRoulette
import app.myzel394.locationtest.ui.components.atoms.SettingsTile
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.list.ListDialog
import com.maxkeppeler.sheets.list.models.ListOption
import com.maxkeppeler.sheets.list.models.ListSelection
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EncoderTile() {
    val scope = rememberCoroutineScope()
    val showDialog = rememberUseCaseState()
    val dataStore = LocalContext.current.dataStore
    val settings = dataStore
        .data
        .collectAsState(initial = AppSettings.getDefaultInstance())
        .value

    fun updateValue(encoder: Int?) {
        scope.launch {
            dataStore.updateData {
                it.setAudioRecorderSettings(
                    it.audioRecorderSettings.setEncoder(encoder)
                )
            }
        }
    }

    ListDialog(
        state = showDialog,
        selection = ListSelection.Single(
            showRadioButtons = true,
            options = IntRange(0, 7).map { index ->
                ListOption(
                    titleText = AudioRecorderSettings.ENCODER_INDEX_TEXT_MAP[index]!!,
                    selected = settings.audioRecorderSettings.encoder == index,
                )
            }.toList()
        ) {index, option ->
            updateValue(index)
        },
    )
    SettingsTile(
        title = "Encoder",
        leading = {
            Icon(
                Icons.Default.Memory,
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
                    text = if (settings.audioRecorderSettings.encoder == null) {
                        "Auto"
                    } else {
                        AudioRecorderSettings.ENCODER_INDEX_TEXT_MAP[settings.audioRecorderSettings.encoder]!!
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