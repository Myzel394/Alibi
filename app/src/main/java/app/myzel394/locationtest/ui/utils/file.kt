package app.myzel394.locationtest.ui.utils

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.io.File

@Composable
fun rememberFileSaverDialog(mimeType: String): ((File) -> Unit) {
    val context = LocalContext.current

    var file: File? = null

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.CreateDocument(mimeType)) {
        it?.let {
            context.contentResolver.openOutputStream(it)?.use { outputStream ->
                file!!.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }

        file = null
    }

    return {
        file = it
        launcher.launch(it.name)
    }
}
