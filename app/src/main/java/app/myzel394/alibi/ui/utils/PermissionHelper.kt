package app.myzel394.alibi.ui.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat

// From @Bnyro
object PermissionHelper {
    fun checkPermissions(context: Context, permissions: Array<String>): Boolean {
        permissions.forEach {
            if (!hasGranted(context, it)) {
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(it),
                    1
                )
                return false
            }
        }
        return true
    }

    fun hasGranted(context: Context, permission: String): Boolean =
        ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    fun hasPermanentlyDenied(context: Context, permission: String): Boolean =
        !hasGranted(context, permission) &&
        !ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, permission)

    fun hasPermanentlyDenied(context: Context, permission: Array<String>): Boolean {
        permission.forEach {
            if (hasPermanentlyDenied(context, it))
                return true
        }
        return false
    }
}

fun Context.openAppSystemSettings() {
    startActivity(Intent().apply {
        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        data = Uri.fromParts("package", packageName, null)
    })
}
