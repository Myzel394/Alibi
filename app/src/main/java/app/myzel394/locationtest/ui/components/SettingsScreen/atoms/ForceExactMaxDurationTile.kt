package app.myzel394.locationtest.ui.components.SettingsScreen.atoms

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import app.myzel394.locationtest.R
import app.myzel394.locationtest.dataStore
import app.myzel394.locationtest.db.AppSettings
import app.myzel394.locationtest.db.AudioRecorderSettings
import app.myzel394.locationtest.ui.components.atoms.ExampleListRoulette
import app.myzel394.locationtest.ui.components.atoms.SettingsTile
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import kotlinx.coroutines.launch


@Composable
fun ForceExactMaxDurationTile() {
    val scope = rememberCoroutineScope()
    val showDialog = rememberUseCaseState()
    val dataStore = LocalContext.current.dataStore
    val settings = dataStore
        .data
        .collectAsState(initial = AppSettings.getDefaultInstance())
        .value

    fun updateValue(forceExactMaxDuration: Boolean) {
        scope.launch {
            dataStore.updateData {
                it.setAudioRecorderSettings(
                    it.audioRecorderSettings.setForceExactMaxDuration(forceExactMaxDuration)
                )
            }
        }
    }


    SettingsTile(
        title = stringResource(R.string.ui_settings_option_forceExactDuration_title),
        description = stringResource(R.string.ui_settings_option_forceExactDuration_description),
        leading = {
            Icon(
                Icons.Default.GraphicEq,
                contentDescription = null,
            )
        },
        trailing = {
            Switch(
                checked = settings.audioRecorderSettings.forceExactMaxDuration,
                onCheckedChange = ::updateValue,
            )
        },
    )
}