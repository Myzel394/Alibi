package app.myzel394.alibi

import android.app.Application
import android.app.NotificationChannel
import android.os.Build
import androidx.compose.ui.res.stringResource

class UpdateSettingsApp: Application() {
    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationHelper.createChannels(this)
        }
    }
}