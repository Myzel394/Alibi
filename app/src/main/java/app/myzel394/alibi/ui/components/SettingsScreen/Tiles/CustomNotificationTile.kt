package app.myzel394.alibi.ui.components.SettingsScreen.Tiles

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.navigation.NavController
import app.myzel394.alibi.R
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.ui.components.atoms.SettingsTile
import app.myzel394.alibi.ui.enums.Screen

@Composable
fun CustomNotificationTile(
    navController: NavController,
    settings: AppSettings,
) {
    val dataStore = LocalContext.current.dataStore

    val label = if (settings.notificationSettings == null)
        stringResource(R.string.ui_settings_option_customNotification_description_setup)
    else stringResource(
        R.string.ui_settings_option_customNotification_description_edit
    )

    SettingsTile(
        firstModifier = Modifier
            .clickable {
                navController.navigate(Screen.CustomRecordingNotifications.route)
            }
            .semantics { contentDescription = label },
        title = stringResource(R.string.ui_settings_option_customNotification_title),
        description = label,
        leading = {
            Icon(
                Icons.Default.Notifications,
                contentDescription = null,
            )
        },
        trailing = {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
            )
        }
    )
}