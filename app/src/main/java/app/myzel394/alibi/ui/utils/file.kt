package app.myzel394.alibi.ui.utils

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.io.File

@Composable
fun rememberFileSaverDialog(mimeType: String): ((File) -> Unit) {
    val context = LocalContext.current

    var file = remember { mutableStateOf<File?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(mimeType)) {
        println("file")
        println(file)
        it?.let {
            context.contentResolver.openOutputStream(it)?.use { outputStream ->
                file.value!!.inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }

        file.value = null
    }

    return {
        println("eich")
        println(it)
        file.value = it
        launcher.launch(it.name)
    }
}
