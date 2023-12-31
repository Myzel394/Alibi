package app.myzel394.alibi.ui.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.io.File

@Composable
fun rememberFileSaverDialog(
    mimeType: String,
    callback: (Uri?) -> Unit = {},
): ((File, String) -> Unit) {
    val context = LocalContext.current

    var file = remember { mutableStateOf<File?>(null) }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(mimeType)) {
            it?.let {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    file.value!!.inputStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }

            file.value = null

            callback(it)
        }

    return { it, name ->
        file.value = it
        launcher.launch(name ?: it.name)
    }
}

@Composable
fun rememberFileSelectorDialog(
    callback: (Uri) -> Unit
): ((String) -> Unit) {
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
            if (it != null) {
                callback(it)
            }
        }

    return { mimeType ->
        launcher.launch(arrayOf(mimeType))
    }
}

@Composable
fun rememberFolderSelectorDialog(
    callback: (Uri?) -> Unit
): (() -> Unit) {
    val launcher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                val uri = it.data?.data

                callback(uri)
            }
        }

    return {
        launcher.launch(Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                        or Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
            )
        })
    }
}
