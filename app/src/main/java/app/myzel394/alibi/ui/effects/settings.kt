package app.myzel394.alibi.ui.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.db.AppSettings

@Composable
fun rememberSettings(): AppSettings {
    return LocalContext.current.dataStore.data.collectAsState(initial = AppSettings.getDefaultInstance()).value
}
