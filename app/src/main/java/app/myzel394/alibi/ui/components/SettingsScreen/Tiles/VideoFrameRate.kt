package app.myzel394.alibi.ui.components.SettingsScreen.Tiles

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
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
import app.myzel394.alibi.db.VideoRecorderSettings
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
fun VideoFrameRate(
    settings: AppSettings,
) {
    val scope = rememberCoroutineScope()
    val showDialog = rememberUseCaseState()
    val dataStore = LocalContext.current.dataStore

    fun updateValue(frameRate: Int?) {
        scope.launch {
            dataStore.updateData {
                it.setVideoRecorderSettings(
                    it.videoRecorderSettings.setTargetFrameRate(frameRate)
                )
            }
        }
    }

    val notNumberLabel = stringResource(R.string.form_error_type_notNumber)
    InputDialog(
        state = showDialog,
        header = Header.Default(
            title = stringResource(R.string.ui_settings_option_videoTargetedFrameRate_title),
            icon = IconSource(
                painter = IconResource.fromImageVector(Icons.Default.Tune).asPainterResource(),
                contentDescription = null,
            )
        ),
        selection = InputSelection(
            input = listOf(
                InputTextField(
                    header = InputHeader(
                        title = stringResource(id = R.string.ui_settings_option_videoTargetedFrameRate_explanation),
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                    ),
                    type = InputTextFieldType.OUTLINED,
                    text = if (settings.videoRecorderSettings.targetFrameRate == null) "" else settings.videoRecorderSettings.targetFrameRate.toString(),
                    validationListener = { text ->
                        val frameRate = text?.toIntOrNull()

                        if (frameRate == null) {
                            return@InputTextField ValidationResult.Invalid(notNumberLabel)
                        }

                        ValidationResult.Valid
                    },
                    key = "framerate",
                )
            ),
        ) { result ->
            val frameRate = result.getString("framerate")?.toIntOrNull() ?: return@InputSelection

            updateValue(frameRate)
        }
    )
    SettingsTile(
        title = stringResource(R.string.ui_settings_option_videoTargetedFrameRate_title),
        leading = {
            Icon(
                Icons.Default.BrokenImage,
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
                if (settings.videoRecorderSettings.targetFrameRate == null)
                    Text(stringResource(R.string.ui_settings_value_auto_label))
                else
                    Text(settings.videoRecorderSettings.targetFrameRate.toString())
            }
        },
        extra = {
            ExampleListRoulette(
                items = VideoRecorderSettings.EXAMPLE_FRAME_RATE_VALUES,
                onItemSelected = ::updateValue,
            ) { frameRate ->
                Text(
                    frameRate?.toString() ?: stringResource(R.string.ui_settings_value_auto_label)
                )
            }
        }
    )
}
