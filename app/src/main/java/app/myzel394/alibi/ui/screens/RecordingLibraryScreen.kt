package app.myzel394.alibi.ui.screens

import android.content.Intent
import android.text.format.Formatter
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.myzel394.alibi.R
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.recording.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingLibraryScreen(settings: AppSettings, onBack: () -> Unit) {
    val context = LocalContext.current
    val library = remember { RecordingLibrary(context.applicationContext) }
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var files by remember { mutableStateOf(emptyList<RecordingFile>()) }
    var errors by remember { mutableStateOf(emptyList<String>()) }
    var loading by remember { mutableStateOf(false) }
    var type by remember { mutableIntStateOf(0) }
    var stage by remember { mutableIntStateOf(0) }
    var playing by remember { mutableStateOf<RecordingFile?>(null) }
    var deleting by remember { mutableStateOf<RecordingFile?>(null) }
    var revision by remember { mutableIntStateOf(0) }
    val states by RecordingRuntime.states.collectAsState()
    val saving by RecordingRuntime.saving.collectAsState()
    val busy = states.isNotEmpty() || saving > 0
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) revision++ }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(settings.saveFolder, settings.lastRecording, states, saving, revision) {
        loading = true
        try {
            val result = withContext(Dispatchers.IO) { library.load(settings) }
            files = result.files; errors = result.errors
        } finally { loading = false }
    }
    fun share(item: RecordingFile) {
        runCatching {
            val uri = library.shareUri(item)
            context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                this.type = item.mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                clipData = android.content.ClipData.newRawUri(item.name, uri)
            }, context.getString(R.string.library_share)))
        }.onFailure { error -> scope.launch { snackbar.showSnackbar(context.getString(R.string.library_open_failed) + " " + (error.localizedMessage ?: "")) } }
    }
    playing?.let { item -> RecordingPlayback(item, library) { playing = null } }
    deleting?.let { item -> AlertDialog(
        onDismissRequest = { deleting = null },
        title = { Text(stringResource(R.string.library_delete_title)) },
        text = { Text(item.name) },
        confirmButton = { TextButton(enabled = !busy, onClick = {
            deleting = null
            scope.launch {
                runCatching { withContext(Dispatchers.IO) { library.delete(item) } }
                    .onFailure { snackbar.showSnackbar(it.localizedMessage ?: context.getString(R.string.library_delete_failed)) }
                revision++
            }
        }) { Text(stringResource(R.string.library_delete)) } },
        dismissButton = { TextButton(onClick = { deleting = null }) { Text(stringResource(R.string.library_cancel)) } },
    ) }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = { TopAppBar(
            title = { Text(stringResource(R.string.library_title)) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.goBack)) } },
            actions = { IconButton(onClick = { revision++ }, enabled = !loading) { Icon(Icons.Default.Refresh, stringResource(R.string.library_refresh)) } },
        ) },
    ) { padding -> Column(Modifier.fillMaxSize().padding(padding)) {
        Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(R.string.library_all, R.string.library_audio, R.string.library_video).forEachIndexed { index, label ->
                FilterChip(selected = type == index, onClick = { type = index }, label = { Text(stringResource(label)) })
            }
        }
        Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(R.string.library_all, R.string.library_fragments, R.string.library_merged).forEachIndexed { index, label ->
                FilterChip(selected = stage == index, onClick = { stage = index }, label = { Text(stringResource(label)) })
            }
        }
        if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        if (busy) Text(stringResource(R.string.library_recording_busy), Modifier.padding(16.dp), style = MaterialTheme.typography.bodySmall)
        errors.forEach { Text(it, Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        val visible = files.filter { (type == 0 || (type == 2) == it.video) && (stage == 0 || (stage == 1) == it.fragment) }
        if (!loading && visible.isEmpty()) Text(stringResource(R.string.library_empty), Modifier.padding(24.dp))
        LazyColumn(Modifier.weight(1f)) {
            items(visible, key = { it.uri.toString() }) { file ->
                ListItem(
                    headlineContent = { Text(file.name) },
                    supportingContent = { Column {
                        Text(stringResource(if (file.video) R.string.library_video else R.string.library_audio) + " · " +
                            stringResource(if (file.fragment) R.string.library_fragments else R.string.library_merged) + " · " + Formatter.formatFileSize(context, file.bytes))
                        Text(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(file.modified)))
                        Text(file.file?.parent ?: file.uri.toString(), maxLines = 2, style = MaterialTheme.typography.labelSmall)
                    } },
                    trailingContent = { Row {
                        IconButton(enabled = !(busy && file.fragment), onClick = { playing = file }) { Icon(Icons.Default.PlayArrow, stringResource(R.string.library_open)) }
                        IconButton(enabled = !(busy && file.fragment), onClick = { share(file) }) { Icon(Icons.Default.Share, stringResource(R.string.library_share)) }
                        IconButton(enabled = !busy, onClick = { deleting = file }) { Icon(Icons.Default.Delete, stringResource(R.string.library_delete)) }
                    } },
                )
                HorizontalDivider()
            }
        }
    } }
}
