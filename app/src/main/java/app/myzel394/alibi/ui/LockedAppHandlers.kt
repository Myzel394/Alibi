package app.myzel394.alibi.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.db.AppSettings

// Handlers that can safely be run when the app is locked (biometric authentication required)
@Composable
fun LockedAppHandlers() {
    val context = LocalContext.current
    val settings = context
        .dataStore
        .data
        .collectAsState(initial = null)
        .value ?: return

    LaunchedEffect(settings.theme) {
        if (!SUPPORTS_DARK_MODE_NATIVELY) {
            val currentValue = AppCompatDelegate.getDefaultNightMode()

            if (settings.theme == AppSettings.Theme.LIGHT && currentValue != AppCompatDelegate.MODE_NIGHT_NO) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else if (settings.theme == AppSettings.Theme.DARK && currentValue != AppCompatDelegate.MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
    }
}