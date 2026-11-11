package app.myzel394.alibi.ui.components.SettingsScreen

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.myzel394.alibi.R
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.recording.RecordingKeyService
import kotlinx.coroutines.launch

@Composable
fun RecordingControlsSettings(settings: AppSettings) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var revision by remember { mutableIntStateOf(0) }
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) revision++ }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    val keyService = remember(revision) {
        val component = ComponentName(context, RecordingKeyService::class.java)
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            ?.split(':')?.any { ComponentName.unflattenFromString(it) == component } == true
    }
    val overlayAllowed = remember(revision) { Settings.canDrawOverlays(context) }
    val permissions = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        val ready = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
            (Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)
        scope.launch { context.dataStore.updateData { it.copy(hardwareShortcutEnabled = ready) } }
    }
    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.shortcut_settings_title), style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.shortcut_enabled), Modifier.weight(1f))
            Switch(checked = settings.hardwareShortcutEnabled, onCheckedChange = { enabled ->
                if (enabled) permissions.launch(buildList {
                    add(Manifest.permission.CAMERA); add(Manifest.permission.RECORD_AUDIO)
                    if (Build.VERSION.SDK_INT >= 33) add(Manifest.permission.POST_NOTIFICATIONS)
                }.toTypedArray())
                else scope.launch { context.dataStore.updateData { it.copy(hardwareShortcutEnabled = false) } }
            })
        }
        Text(stringResource(R.string.shortcut_service_description), style = MaterialTheme.typography.bodySmall)
        OutlinedButton(onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }) {
            Text(stringResource(if (keyService) R.string.shortcut_accessibility_ready else R.string.shortcut_accessibility_open))
        }
        if (Build.MANUFACTURER.equals("Xiaomi", ignoreCase = true)) {
            Text(stringResource(R.string.shortcut_xiaomi_hint), style = MaterialTheme.typography.bodySmall)
            OutlinedButton(onClick = {
                val permissionPage = Intent("miui.intent.action.APP_PERM_EDITOR").apply {
                    component = ComponentName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity")
                    putExtra("extra_pkgname", context.packageName)
                }
                runCatching { context.startActivity(permissionPage) }.onFailure {
                    context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}")))
                }
            }) { Text(stringResource(R.string.shortcut_background_permissions)) }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.indicator_enabled), Modifier.weight(1f))
            Switch(checked = settings.recordingIndicatorEnabled, onCheckedChange = { enabled ->
                scope.launch { context.dataStore.updateData { it.copy(recordingIndicatorEnabled = enabled) } }
                if (enabled && !overlayAllowed) context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")))
            })
        }
        Text(stringResource(R.string.indicator_description), style = MaterialTheme.typography.bodySmall)
        if (settings.recordingIndicatorEnabled && !overlayAllowed) OutlinedButton(onClick = {
            context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}")))
        }) { Text(stringResource(R.string.indicator_permission)) }
    }
}
