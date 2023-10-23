package app.myzel394.alibi.ui.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat

// From @Bnyro
object PermissionHelper {
    fun checkPermission(context: Context, permission: String): Boolean {
        if (!hasGranted(context, permission)) {
            ActivityCompat.requestPermissions(
                context as Activity,
                arrayOf(permission),
                1
            )
            return false
        }
        return true
    }

    fun hasGranted(context: Context, permission: String): Boolean =
        ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}

fun Context.openAppSystemSettings() {
    startActivity(Intent().apply {
        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        data = Uri.fromParts("package", packageName, null)
    })
}

fun Context.openNotificationsSettings() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        startActivity(Intent().apply {
            action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        })
    } else {
        startActivity(Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", packageName, null)
        })
    }
}
