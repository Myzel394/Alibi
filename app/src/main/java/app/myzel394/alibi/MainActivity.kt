package app.myzel394.alibi

import android.content.Context
import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.datastore.dataStore
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.db.AppSettingsSerializer
import app.myzel394.alibi.ui.AsLockedApp
import app.myzel394.alibi.ui.Navigation
import app.myzel394.alibi.ui.SUPPORTS_DARK_MODE_NATIVELY
import app.myzel394.alibi.ui.theme.AlibiTheme

const val SETTINGS_FILE = "settings.json"
val Context.dataStore by dataStore(
    fileName = SETTINGS_FILE,
    serializer = AppSettingsSerializer()
)

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            AlibiTheme {
                AsLockedApp {
                    Navigation()
                }
            }
        }
    }
}
