package app.myzel394.alibi.ui.effects

import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberOpenUri(): (uri: Uri) -> Unit {
    val context = LocalContext.current

    return fun(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setDataAndType(uri, DocumentsContract.Document.MIME_TYPE_DIR)
        }

        context.startActivity(intent)
    }
}

