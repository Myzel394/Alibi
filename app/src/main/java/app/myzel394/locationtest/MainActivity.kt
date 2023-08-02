package app.myzel394.locationtest

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.datastore.dataStore
import app.myzel394.locationtest.db.AppSettingsSerializer
import app.myzel394.locationtest.ui.Navigation
import app.myzel394.locationtest.ui.theme.LocationTestTheme

const val SETTINGS_FILE = "settings.json"
val Context.dataStore by dataStore(
    fileName = SETTINGS_FILE,
    serializer = AppSettingsSerializer()
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LocationTestTheme {
                Navigation()
            }
        }
    }
}
