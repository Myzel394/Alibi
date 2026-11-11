package app.myzel394.alibi.ui.screens

import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import app.myzel394.alibi.R
import app.myzel394.alibi.recording.RecordingFile
import app.myzel394.alibi.recording.RecordingLibrary

/** In-app playback avoids requiring another media player or exposing private file paths. */
@Composable
fun RecordingPlayback(item: RecordingFile, library: RecordingLibrary, onClose: () -> Unit) {
    val context = LocalContext.current
    var failed by remember(item.uri) { mutableStateOf(false) }
    val player = remember(item.uri) { VideoView(context) }
    DisposableEffect(player) { onDispose { player.stopPlayback() } }
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxWidth().padding(16.dp), shape = MaterialTheme.shapes.large) {
            Column(Modifier.padding(16.dp)) {
                Text(item.name, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))
                if (failed) Text(stringResource(R.string.library_playback_failed), color = MaterialTheme.colorScheme.error)
                else AndroidView(
                    modifier = Modifier.fillMaxWidth().height(if (item.video) 320.dp else 140.dp).background(Color.Black),
                    factory = {
                        player.apply {
                            val controls = MediaController(context)
                            controls.setAnchorView(this)
                            setMediaController(controls)
                            setOnPreparedListener { media -> media.start(); controls.show(0) }
                            setOnErrorListener { _, _, _ -> failed = true; true }
                            setVideoURI(library.shareUri(item))
                        }
                    },
                )
                TextButton(onClick = onClose, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.library_close)) }
            }
        }
    }
}
