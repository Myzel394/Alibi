package app.myzel394.alibi

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.datastore.dataStore
import app.myzel394.alibi.db.AppSettingsSerializer
import app.myzel394.alibi.ui.AsLockedApp
import app.myzel394.alibi.ui.LockedAppHandlers
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
                LockedAppHandlers()

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            MaterialTheme.colorScheme.background
                        )
                ) {
                    AsLockedApp {
                        Navigation()
                    }
                }
            }
        }
    }
}
