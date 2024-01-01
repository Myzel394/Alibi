package app.myzel394.alibi.helpers

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build

data class Doctor(
    val context: Context
) {
    fun checkIfFileSaverDialogIsAvailable(): Boolean {
        // Since API 30, we can't query other packages so easily anymore
        // (see https://developer.android.com/training/package-visibility).
        // For now, we assume the user has a file saver app installed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return true
        }

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        if (intent.resolveActivity(context.packageManager) != null) {
            return true

        }

        val results =
            context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

        if (results.isNotEmpty()) {
            return true;
        }

        return false
    }
}