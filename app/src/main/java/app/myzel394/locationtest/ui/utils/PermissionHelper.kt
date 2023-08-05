package app.myzel394.locationtest.ui.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

// From @Bnyro
object PermissionHelper {
    fun checkPermissions(context: Context, permissions: Array<String>): Boolean {
        permissions.forEach {
            if (!hasPermission(context, it)) {
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

    fun hasPermission(context: Context, permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
}
