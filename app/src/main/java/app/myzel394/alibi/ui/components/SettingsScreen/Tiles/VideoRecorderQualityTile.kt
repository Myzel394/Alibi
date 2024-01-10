package app.myzel394.alibi.ui.components.SettingsScreen.Tiles

import android.media.MediaRecorder
import androidx.camera.video.Quality
import androidx.camera.video.Recorder
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HighQuality
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
import app.myzel394.alibi.db.VideoRecorderSettings
import app.myzel394.alibi.ui.components.atoms.ExampleListRoulette
import app.myzel394.alibi.ui.components.atoms.SettingsTile
import app.myzel394.alibi.ui.utils.IconResource
import com.maxkeppeker.sheets.core.models.base.Header
import com.maxkeppeker.sheets.core.models.base.IconSource
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.input.models.InputHeader
import com.maxkeppeler.sheets.list.ListDialog
import com.maxkeppeler.sheets.list.models.ListOption
import com.maxkeppeler.sheets.list.models.ListSelection
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoRecorderQualityTile(
    settings: AppSettings,
) {
    val QUALITY_NAME_TEXT_MAP = mapOf<Quality, String>(
        Quality.HIGHEST to stringResource(R.string.ui_settings_value_videoQuality_values_highest),
        Quality.UHD to stringResource(R.string.ui_settings_value_videoQuality_values_uhd),
        Quality.FHD to stringResource(R.string.ui_settings_value_videoQuality_values_fhd),
        Quality.HD to stringResource(R.string.ui_settings_value_videoQuality_values_hd),
        Quality.SD to stringResource(R.string.ui_settings_value_videoQuality_values_sd),
        Quality.LOWEST to stringResource(R.string.ui_settings_value_videoQuality_values_lowest),
    )

    val scope = rememberCoroutineScope()
    val showDialog = rememberUseCaseState()
    val dataStore = LocalContext.current.dataStore

    fun updateValue(quality: Quality?) {
        scope.launch {
            dataStore.updateData {
                it.setVideoRecorderSettings(
                    it.videoRecorderSettings.setQuality(quality)
                )
            }
        }
    }

    ListDialog(
        state = showDialog,
        header = Header.Default(
            title = stringResource(R.string.ui_settings_option_videoQualityTile_title),
            icon = IconSource(
                painter = IconResource.fromImageVector(Icons.Default.HighQuality)
                    .asPainterResource(),
                contentDescription = null,
            ),
        ),
        selection = ListSelection.Single(
            showRadioButtons = true,
            options = VideoRecorderSettings.AVAILABLE_QUALITIES.map { quality ->
                ListOption(
                    titleText = QUALITY_NAME_TEXT_MAP[quality]!!,
                    selected = settings.videoRecorderSettings.quality == quality.toString(),
                )
            }.toList()
        ) { index, _ ->
            val quality = VideoRecorderSettings.AVAILABLE_QUALITIES[index]

            updateValue(quality)
        },
    )
    SettingsTile(
        title = stringResource(R.string.ui_settings_option_videoQualityTile_title),
        leading = {
            Icon(
                Icons.Default.HighQuality,
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
                    QUALITY_NAME_TEXT_MAP[settings.videoRecorderSettings.getQuality()]
                        ?: stringResource(
                            R.string.ui_settings_value_auto_label
                        )
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
        },
    )
}