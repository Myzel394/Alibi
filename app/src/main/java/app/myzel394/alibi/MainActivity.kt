package app.myzel394.alibi

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.datastore.dataStore
import app.myzel394.alibi.db.AppSettingsSerializer
import app.myzel394.alibi.ui.Navigation
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
                Navigation()
            }
        }
    }
}
