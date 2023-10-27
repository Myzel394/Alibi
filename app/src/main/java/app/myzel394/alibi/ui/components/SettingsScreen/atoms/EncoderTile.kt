package app.myzel394.alibi.ui.components.SettingsScreen.atoms

import android.media.MediaRecorder
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
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
fun EncoderTile(
    snackbarHostState: SnackbarHostState,
    settings: AppSettings,
) {
    val scope = rememberCoroutineScope()
    val showDialog = rememberUseCaseState()
    val dataStore = LocalContext.current.dataStore

    val updatedOutputFormatLabel =
        stringResource(R.string.ui_settings_option_encoder_extra_outputFormatChanged)

    fun updateValue(encoder: Int?) {
        scope.launch {
            val isCompatible = if (encoder == null || encoder == MediaRecorder.AudioEncoder.DEFAULT)
                true
            else settings.audioRecorderSettings.isEncoderCompatible(encoder)

            dataStore.updateData {
                it.setAudioRecorderSettings(
                    it.audioRecorderSettings.setEncoder(encoder)
                )
            }

            if (!isCompatible) {
                dataStore.updateData {
                    it.setAudioRecorderSettings(
                        it.audioRecorderSettings.setOutputFormat(null)
                    )
                }

                snackbarHostState.showSnackbar(
                    message = updatedOutputFormatLabel,
                    withDismissAction = true,
                    duration = SnackbarDuration.Long,
                )
            }
        }
    }

    ListDialog(
        state = showDialog,
        header = Header.Default(
            title = stringResource(R.string.ui_settings_option_encoder_title),
            icon = IconSource(
                painter = IconResource.fromImageVector(Icons.Default.Memory).asPainterResource(),
                contentDescription = null,
            )
        ),
        selection = ListSelection.Single(
            showRadioButtons = true,
            options = IntRange(0, 7).map { index ->
                ListOption(
                    titleText = AudioRecorderSettings.ENCODER_INDEX_TEXT_MAP[index]!!,
                    selected = settings.audioRecorderSettings.encoder == index,
                )
            }.toList()
        ) { index, _ ->
            updateValue(index)
        },
    )
    SettingsTile(
        title = stringResource(R.string.ui_settings_option_encoder_title),
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
                        stringResource(R.string.ui_settings_value_auto_label)
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
                Text(stringResource(R.string.ui_settings_value_auto_label))
            }
        }
    )
}