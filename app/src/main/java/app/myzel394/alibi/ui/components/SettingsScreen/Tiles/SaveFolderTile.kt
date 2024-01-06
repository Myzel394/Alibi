package app.myzel394.alibi.ui.components.SettingsScreen.Tiles

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import app.myzel394.alibi.R
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.helpers.AudioBatchesFolder
import app.myzel394.alibi.helpers.VideoBatchesFolder
import app.myzel394.alibi.ui.AUDIO_RECORDING_BATCHES_SUBFOLDER_NAME
import app.myzel394.alibi.ui.MEDIA_SUBFOLDER_NAME
import app.myzel394.alibi.ui.RECORDER_MEDIA_SELECTED_VALUE
import app.myzel394.alibi.ui.SHEET_BOTTOM_OFFSET
import app.myzel394.alibi.ui.SUPPORTS_SAVING_VIDEOS_IN_CUSTOM_FOLDERS
import app.myzel394.alibi.ui.SUPPORTS_SCOPED_STORAGE
import app.myzel394.alibi.ui.VIDEO_RECORDING_BATCHES_SUBFOLDER_NAME
import app.myzel394.alibi.ui.components.SettingsScreen.atoms.FolderBreadcrumbs
import app.myzel394.alibi.ui.components.atoms.MessageBox
import app.myzel394.alibi.ui.components.atoms.MessageType
import app.myzel394.alibi.ui.components.atoms.PermissionRequester
import app.myzel394.alibi.ui.components.atoms.SettingsTile
import app.myzel394.alibi.ui.effects.rememberOpenUri
import app.myzel394.alibi.ui.utils.PermissionHelper
import app.myzel394.alibi.ui.utils.rememberFolderSelectorDialog
import kotlinx.coroutines.launch
import java.net.URLDecoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveFolderTile(
    settings: AppSettings,
    snackbarHostState: SnackbarHostState,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val dataStore = context.dataStore

    val successMessage = stringResource(R.string.ui_settings_option_saveFolder_success)
    fun updateValue(path: String?) {
        if (settings.saveFolder != null && settings.saveFolder != RECORDER_MEDIA_SELECTED_VALUE) {
            runCatching {
                context.contentResolver.releasePersistableUriPermission(
                    Uri.parse(settings.saveFolder),
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            }
        }

        if (path != null && path != RECORDER_MEDIA_SELECTED_VALUE) {
            context.contentResolver.takePersistableUriPermission(
                Uri.parse(path),
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        }

        scope.launch {
            dataStore.updateData {
                it.setSaveFolder(path)
            }
            snackbarHostState.showSnackbar(
                message = successMessage,
                duration = SnackbarDuration.Short,
            )
        }
    }

    var selectionVisible by remember { mutableStateOf(false) }
    val selectionSheetState = rememberModalBottomSheetState(true)

    fun hideSheet() {
        scope.launch {
            selectionSheetState.hide()
            selectionVisible = false
        }
    }

    if (selectionVisible) {
        SelectionSheet(
            sheetState = selectionSheetState,
            updateValue = { path ->
                updateValue(path)
                hideSheet()
            },
            onDismiss = ::hideSheet,
        )
    }

    var showDCIMFolderHelpSheet by remember { mutableStateOf(false) }

    if (showDCIMFolderHelpSheet) {
        MediaFoldersExplanationDialog(
            onDismiss = {
                showDCIMFolderHelpSheet = false
            }
        )
    }

    SettingsTile(
        title = stringResource(R.string.ui_settings_option_saveFolder_title),
        description = stringResource(R.string.ui_settings_option_saveFolder_explanation),
        leading = {
            Icon(
                Icons.Default.InsertDriveFile,
                contentDescription = null,
            )
        },
        trailing = {
            Button(
                onClick = {
                    scope.launch {
                        selectionVisible = true
                    }
                },
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(
                    text = stringResource(R.string.ui_settings_option_saveFolder_action_select_label),
                )
            }
        },
        extra = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(
                        R.string.form_value_selected,
                        when (settings.saveFolder) {
                            RECORDER_MEDIA_SELECTED_VALUE -> stringResource(R.string.ui_settings_option_saveFolder_dcimValue)
                            null -> stringResource(R.string.ui_settings_option_saveFolder_defaultValue)
                            else -> splitPath(settings.saveFolder).joinToString(" > ")
                        }
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )


                val openFolder = rememberOpenUri()

                when (settings.saveFolder) {
                    null -> {}
                    RECORDER_MEDIA_SELECTED_VALUE -> {
                        Button(
                            onClick = {
                                showDCIMFolderHelpSheet = true
                            },
                            shape = MaterialTheme.shapes.small,
                            contentPadding = ButtonDefaults.TextButtonContentPadding,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            ),
                        ) {
                            Text(
                                stringResource(R.string.ui_settings_option_saveFolder_explainMediaFolder_label),
                                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                            )
                        }
                    }
                    // Custom folder
                    else ->
                        // Doesn't seem to reliably work on all devices, 30 & 33
                        // has been tested; so we just show the button for versions
                        // above 30
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                            Button(
                                onClick = {
                                    openFolder(
                                        DocumentFile.fromTreeUri(
                                            context,
                                            settings.saveFolder.toUri(),
                                        )!!.uri
                                    )
                                },
                                shape = MaterialTheme.shapes.small,
                                contentPadding = ButtonDefaults.TextButtonContentPadding,
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                ),
                            ) {
                                Text(
                                    stringResource(R.string.ui_settings_option_saveFolder_openFolder_label),
                                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                                )
                            }
                }
            }
        }
    )
}

@Composable
fun MediaFoldersExplanationDialog(
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val openFolder = rememberOpenUri()

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.PermMedia,
                contentDescription = null,
            )
        },
        title = {
            Text(stringResource(R.string.ui_settings_option_saveFolder_explainMediaFolder_label))
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_close_neutral_label))
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp),
            ) {
                Text(
                    stringResource(R.string.ui_settings_option_saveFolder_explainMediaFolder_generalExplanation),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                // I tried adding support for opening the folder directly by tapping on the
                // breadcrumbs, but couldn't get it to work.
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = null,
                        )
                        FolderBreadcrumbs(
                            folders = listOf(
                                if (SUPPORTS_SCOPED_STORAGE)
                                    AudioBatchesFolder.BASE_SCOPED_STORAGE_RELATIVE_PATH
                                else
                                    AudioBatchesFolder.BASE_LEGACY_STORAGE_FOLDER,
                                MEDIA_SUBFOLDER_NAME
                            )
                        )
                        Box {}
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(16.dp)
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                        )
                        FolderBreadcrumbs(
                            folders = listOf(
                                if (SUPPORTS_SCOPED_STORAGE)
                                    VideoBatchesFolder.BASE_SCOPED_STORAGE_RELATIVE_PATH
                                else
                                    VideoBatchesFolder.BASE_LEGACY_STORAGE_FOLDER,
                                MEDIA_SUBFOLDER_NAME
                            )
                        )
                        Box {}
                    }
                }
                Text(
                    stringResource(
                        R.string.ui_settings_option_saveFolder_explainMediaFolder_subfoldersExplanation,
                        AUDIO_RECORDING_BATCHES_SUBFOLDER_NAME,
                        VIDEO_RECORDING_BATCHES_SUBFOLDER_NAME
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionSheet(
    sheetState: SheetState,
    updateValue: (String?) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

    var showCustomFolderWarning by remember { mutableStateOf(false) }

    val selectFolder = rememberFolderSelectorDialog { folder ->
        if (folder == null) {
            return@rememberFolderSelectorDialog
        }

        updateValue(folder.toString())
    }

    if (showCustomFolderWarning) {
        CustomFolderWarningDialog(
            onDismiss = {
                showCustomFolderWarning = false
            },
            onConfirm = {
                showCustomFolderWarning = false
                selectFolder()
            },
        )
    }

    var showExternalPermissionRequired by remember { mutableStateOf(false) }

    if (showExternalPermissionRequired) {
        ExternalPermissionRequiredDialog(
            onDismiss = {
                showExternalPermissionRequired = false
            },
            onGranted = {
                showExternalPermissionRequired = false
                updateValue(RECORDER_MEDIA_SELECTED_VALUE)
            },
        )
    }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = SHEET_BOTTOM_OFFSET),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text(
                stringResource(R.string.ui_settings_option_saveFolder_title),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )

            SelectionButton(
                label = stringResource(R.string.ui_settings_option_saveFolder_action_default_label),
                icon = Icons.Default.Lock,
                onClick = {
                    updateValue(null)
                },
            )

            Divider()

            SelectionButton(
                label = stringResource(R.string.ui_settings_option_saveFolder_action_dcim_label),
                icon = Icons.Default.PermMedia,
                onClick = {
                    if (
                        SUPPORTS_SCOPED_STORAGE ||
                        PermissionHelper.hasGranted(
                            context,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    ) {
                        updateValue(RECORDER_MEDIA_SELECTED_VALUE)
                    } else {
                        showExternalPermissionRequired = true
                    }
                },
            )

            Divider()

            Column {
                SelectionButton(
                    label = stringResource(R.string.ui_settings_option_saveFolder_action_custom_label),
                    icon = Icons.Default.Folder,
                    onClick = {
                        showCustomFolderWarning = true
                    },
                )
                if (!SUPPORTS_SAVING_VIDEOS_IN_CUSTOM_FOLDERS) {
                    Column(
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        MessageBox(
                            type = MessageType.INFO,
                            message = stringResource(R.string.ui_settings_option_saveFolder_videoUnsupported),
                        )
                        Text(
                            stringResource(R.string.ui_minApiRequired, 8, 26),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SelectionButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(MaterialTheme.shapes.medium)
            .semantics {
                contentDescription = label
            }
            .clickable {
                onClick()
            }
            .padding(horizontal = 16.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize),
        )
        Text(label)
        Box {}
    }
}

@Composable
fun CustomFolderWarningDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    val title = stringResource(R.string.ui_settings_option_saveFolder_warning_title)
    val text = stringResource(R.string.ui_settings_option_saveFolder_warning_text)

    AlertDialog(
        icon = {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
            )
        },
        onDismissRequest = onDismiss,
        title = {
            Text(text = title)
        },
        text = {
            Text(text = text)
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.ui_settings_option_saveFolder_warning_action_confirm),
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                contentPadding = ButtonDefaults.TextButtonWithIconContentPadding,
            ) {
                Icon(
                    Icons.Default.Cancel,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                )
                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.dialog_close_cancel_label))
            }
        }
    )
}

@Composable
fun ExternalPermissionRequiredDialog(
    onDismiss: () -> Unit,
    onGranted: () -> Unit,
) {
    PermissionRequester(
        icon = Icons.Default.PermMedia,
        permission = Manifest.permission.READ_EXTERNAL_STORAGE,
        onPermissionAvailable = onGranted,
    ) { trigger ->
        AlertDialog(
            icon = {
                Icon(
                    Icons.Default.PermMedia,
                    contentDescription = null,
                )
            },
            onDismissRequest = onDismiss,
            title = {
                Text(
                    stringResource(R.string.ui_settings_option_saveFolder_externalPermissionRequired_title),
                )
            },
            text = {
                Text(
                    stringResource(R.string.ui_settings_option_saveFolder_externalPermissionRequired_text),
                )
            },
            confirmButton = {
                Button(
                    onClick = trigger,
                ) {
                    Text(
                        stringResource(R.string.ui_settings_option_saveFolder_externalPermissionRequired_action_confirm),
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    contentPadding = ButtonDefaults.TextButtonWithIconContentPadding,
                ) {
                    Icon(
                        Icons.Default.Cancel,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                    )
                    Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                    Text(stringResource(R.string.dialog_close_cancel_label))
                }
            }
        )
    }
}


fun splitPath(path: String): List<String> {
    return try {
        URLDecoder
            .decode(path, "UTF-8")
            .split(":", limit = 3)[2]
            .split("/")
    } catch (e: Exception) {
        listOf(path)
    }
}
