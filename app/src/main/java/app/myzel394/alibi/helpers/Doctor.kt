package app.myzel394.alibi.helpers

import android.content.Context
import android.content.Intent

data class Doctor(
    val context: Context
) {
    fun checkIfFileSaverDialogIsAvailable(): Boolean {
        val fileSaver = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)

        return fileSaver.resolveActivity(context.packageManager) != null
    }
}