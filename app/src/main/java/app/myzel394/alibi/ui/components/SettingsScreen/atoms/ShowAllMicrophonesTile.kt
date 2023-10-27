package app.myzel394.alibi.ui.components.SettingsScreen.atoms

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MicExternalOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import app.myzel394.alibi.R
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.ui.components.atoms.SettingsTile
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import kotlinx.coroutines.launch


@Composable
fun ShowAllMicrophonesTile(
    settings: AppSettings,
) {
    val scope = rememberCoroutineScope()
    val dataStore = LocalContext.current.dataStore

    fun updateValue(showAllMicrophones: Boolean) {
        scope.launch {
            dataStore.updateData {
                it.setAudioRecorderSettings(
                    it.audioRecorderSettings.setShowAllMicrophones(showAllMicrophones)
                )
            }
        }
    }


    SettingsTile(
        title = stringResource(R.string.ui_settings_option_showAllMicrophones_title),
        description = stringResource(R.string.ui_settings_option_showAllMicrophones_description),
        leading = {
            Icon(
                Icons.Default.MicExternalOn,
                contentDescription = null,
            )
        },
        trailing = {
            Switch(
                checked = settings.audioRecorderSettings.showAllMicrophones,
                onCheckedChange = ::updateValue,
            )
        },
    )
}