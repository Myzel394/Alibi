package app.myzel394.alibi.ui.components.SettingsScreen.Tiles

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.magnifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.myzel394.alibi.R
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.db.AppLockSettings
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.helpers.AppLockHelper
import app.myzel394.alibi.ui.components.atoms.SettingsTile
import kotlinx.coroutines.launch

@Composable
fun EnableAppLockTile(
    settings: AppSettings,
) {
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    val dataStore = context.dataStore

    val appLockSupport = AppLockHelper.getSupportType(context)

    if (appLockSupport === AppLockHelper.SupportType.UNAVAILABLE) {
        return
    }

    SettingsTile(
        title = stringResource(R.string.ui_settings_option_enableAppLock_title),
        description = stringResource(R.string.ui_settings_option_enableAppLock_description),
        tertiaryLine = {
            if (appLockSupport === AppLockHelper.SupportType.NONE_ENROLLED) {
                Text(
                    stringResource(R.string.ui_settings_option_enableAppLock_enrollmentRequired),
                    color = Color.Yellow,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        },
        leading = {
            Icon(
                Icons.Default.Fingerprint,
                contentDescription = null,
            )
        },
        trailing = {
            val title = stringResource(R.string.identityVerificationRequired_title)
            val subtitle = stringResource(R.string.identityVerificationRequired_subtitle)

            Switch(
                checked = settings.isAppLockEnabled(),
                enabled = appLockSupport === AppLockHelper.SupportType.AVAILABLE,
                onCheckedChange = {
                    scope.launch {
                        val authenticationSuccessful = AppLockHelper.authenticate(
                            context,
                            title = title,
                            subtitle = subtitle,
                        ).await()

                        if (!authenticationSuccessful) {
                            return@launch
                        }

                        dataStore.updateData {
                            it.setAppLockSettings(
                                if (it.appLockSettings == null)
                                    AppLockSettings.getDefaultInstance()
                                else
                                    null
                            )
                        }
                    }
                }
            )
        }
    )
}