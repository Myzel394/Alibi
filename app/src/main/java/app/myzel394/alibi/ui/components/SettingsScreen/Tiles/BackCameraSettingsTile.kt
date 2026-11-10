package app.myzel394.alibi.ui.components.SettingsScreen.Tiles

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import app.myzel394.alibi.R
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.db.CameraSelection
import app.myzel394.alibi.ui.components.atoms.SettingsTile
import app.myzel394.alibi.ui.utils.CameraInfo
import kotlinx.coroutines.launch

@Composable
fun BackCameraSettingsTile(settings: AppSettings) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var open by remember { mutableStateOf(false) }
    var cameras by remember { mutableStateOf(emptyList<CameraInfo>()) }
    var revision by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { revision++ }
    LaunchedEffect(open, revision) {
        if (!open) return@LaunchedEffect
        loading = true
        runCatching { CameraInfo.querySelectableCameras(context) }
            .onSuccess { cameras = it.filter { camera -> camera.lens == CameraInfo.Lens.BACK }; failed = false }
            .onFailure { cameras = emptyList(); failed = true }
        loading = false
    }
    fun select(selection: CameraSelection?) {
        scope.launch { context.dataStore.updateData { it.setVideoRecorderSettings(it.videoRecorderSettings.copy(backCamera = selection)) } }
        open = false
    }
    val selected = settings.videoRecorderSettings.backCamera
    val label = if (selected == null) stringResource(R.string.camera_auto)
        else cameras.find { it.selection == selected }?.label(context)
            ?: stringResource(R.string.camera_selected_id, selected.physicalId ?: selected.logicalId)
    if (open) AlertDialog(
        onDismissRequest = { open = false },
        title = { Text(stringResource(R.string.camera_back_selection)) },
        text = { Column {
            Text(stringResource(R.string.camera_back_selection_hint), style = MaterialTheme.typography.bodySmall)
            if (loading) LinearProgressIndicator(Modifier.fillMaxWidth().padding(vertical = 8.dp))
            if (failed) Text(stringResource(R.string.camera_catalog_failed), color = MaterialTheme.colorScheme.error)
            LazyColumn(Modifier.heightIn(max = 420.dp)) {
                item { ListItem(
                    headlineContent = { Text(stringResource(R.string.camera_auto)) },
                    leadingContent = { RadioButton(selected == null, onClick = { select(null) }) },
                    modifier = Modifier.clickable { select(null) },
                ) }
                items(cameras, key = { it.selectionKey }) { camera -> ListItem(
                    headlineContent = { Text(camera.label(context)) },
                    supportingContent = { Text(stringResource(if (camera.physicalId != null) R.string.camera_physical_lens else R.string.camera_logical_lens)) },
                    leadingContent = { RadioButton(camera.selection == selected, onClick = { select(camera.selection) }) },
                    modifier = Modifier.clickable { select(camera.selection) },
                ) }
            }
            if (!loading && cameras.isEmpty()) Text(stringResource(R.string.camera_no_back_cameras))
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                TextButton(onClick = { permission.launch(Manifest.permission.CAMERA) }) { Text(stringResource(R.string.camera_grant_permission)) }
        } },
        confirmButton = { TextButton(onClick = { revision++ }) { Text(stringResource(R.string.library_refresh)) } },
        dismissButton = { TextButton(onClick = { open = false }) { Text(stringResource(R.string.library_close)) } },
    )
    SettingsTile(
        title = stringResource(R.string.camera_back_selection),
        leading = { Icon(Icons.Default.CameraAlt, null) },
        trailing = { FilledTonalButton(onClick = { open = true }) { Text(label, Modifier.widthIn(max = 200.dp), maxLines = 2, overflow = TextOverflow.Ellipsis) } },
    )
}
