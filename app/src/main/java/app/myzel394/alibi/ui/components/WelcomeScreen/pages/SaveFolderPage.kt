package app.myzel394.alibi.ui.components.WelcomeScreen.pages

import android.Manifest
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.myzel394.alibi.R
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.helpers.BatchesFolder
import app.myzel394.alibi.helpers.VideoBatchesFolder
import app.myzel394.alibi.ui.BIG_PRIMARY_BUTTON_SIZE
import app.myzel394.alibi.ui.RECORDER_MEDIA_SELECTED_VALUE
import app.myzel394.alibi.ui.SUPPORTS_SCOPED_STORAGE
import app.myzel394.alibi.ui.components.WelcomeScreen.atoms.SaveFolderSelection
import app.myzel394.alibi.ui.components.atoms.PermissionRequester
import app.myzel394.alibi.ui.utils.rememberFolderSelectorDialog
import kotlin.concurrent.thread

@Composable
fun SaveFolderPage(
    onBack: () -> Unit,
    onContinue: (saveFolder: String?) -> Unit,
    appSettings: AppSettings,
) {
    var saveFolder by rememberSaveable { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    var isLowOnStorage by rememberSaveable {
        mutableStateOf(false)
    }
    // Fetching this synchronously results in the UI being blocked.
    // Instead, we fetch this in a different thread and update the state when we have the result.
    LaunchedEffect(appSettings, context) {
        thread {
            val availableBytes = VideoBatchesFolder.viaInternalFolder(context).getAvailableBytes()

            if (availableBytes == null) {
                isLowOnStorage = false
                return@thread
            }

            val bytesPerMinute = BatchesFolder.requiredBytesForOneMinuteOfRecording(appSettings)
            val requiredBytes = appSettings.maxDuration / 1000 / 60 * bytesPerMinute

            // Allow for a 10% margin of error
            isLowOnStorage = availableBytes < requiredBytes
        }
    }

    LaunchedEffect(isLowOnStorage, appSettings.maxDuration) {
        if (isLowOnStorage) {
            if (saveFolder == null) {
                saveFolder = RECORDER_MEDIA_SELECTED_VALUE
            }
        } else {
            saveFolder = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Column(
            modifier = Modifier
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                Icons.AutoMirrored.Filled.InsertDriveFile,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(128.dp),
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                stringResource(R.string.ui_welcome_saveFolder_title),
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                stringResource(R.string.ui_welcome_saveFolder_message),
                fontStyle = MaterialTheme.typography.bodySmall.fontStyle,
                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                color = MaterialTheme.typography.bodySmall.color,
            )
        }
        Spacer(modifier = Modifier.height(40.dp))
        Box(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .padding(horizontal = 16.dp)
        ) {
            SaveFolderSelection(
                saveFolder = saveFolder,
                isLowOnStorage = isLowOnStorage,
                onSaveFolderChange = { saveFolder = it },
            )
        }
        Spacer(modifier = Modifier.height(40.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(BIG_PRIMARY_BUTTON_SIZE),
            ) {
                Icon(
                    Icons.Default.ChevronLeft,
                    contentDescription = null,
                )
            }

            var showError by rememberSaveable { mutableStateOf(false) }

            if (showError) {
                _FolderInaccessibleDialog(
                    onClose = {
                        showError = false
                    }
                )
            }

            PermissionRequester(
                permission = Manifest.permission.WRITE_EXTERNAL_STORAGE,
                icon = Icons.AutoMirrored.Filled.InsertDriveFile,
                onPermissionAvailable = { onContinue(saveFolder) },
            ) { requestWritePermission ->
                val selectFolder = rememberFolderSelectorDialog { folder ->
                    if (folder == null) {
                        return@rememberFolderSelectorDialog
                    }

                    context.contentResolver.takePersistableUriPermission(
                        folder,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )

                    if (BatchesFolder.canAccessFolder(context, folder)) {
                        onContinue(folder.toString())
                    } else {
                        showError = true

                        runCatching {
                            context.contentResolver.releasePersistableUriPermission(
                                folder,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                            )
                        }
                    }
                }
                var showCustomFolderHint by rememberSaveable { mutableStateOf(false) }

                if (showCustomFolderHint) {
                    _CustomFolderDialog(
                        onAbort = { showCustomFolderHint = false },
                        onOk = {
                            showCustomFolderHint = false
                            selectFolder()
                        },
                    )
                }

                Button(
                    onClick = {
                        when (saveFolder) {
                            null -> onContinue(saveFolder)
                            RECORDER_MEDIA_SELECTED_VALUE -> {
                                if (SUPPORTS_SCOPED_STORAGE) {
                                    onContinue(saveFolder)
                                } else {
                                    requestWritePermission()
                                }
                            }

                            else -> {
                                showCustomFolderHint = true
                            }
                        }
                    },
                    enabled = if (saveFolder == null) !isLowOnStorage else true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(BIG_PRIMARY_BUTTON_SIZE),
                    contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                ) {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                    Text(stringResource(R.string.continue_label))
                }
            }
        }
    }
}

@Composable
fun _FolderInaccessibleDialog(
    onClose: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onClose,
        icon = {
            Icon(
                Icons.Default.Error,
                contentDescription = null,
            )
        },
        title = {
            Text(stringResource(R.string.ui_error_occurred_title))
        },
        confirmButton = {
            Button(onClick = onClose) {
                Text(stringResource(R.string.dialog_close_neutral_label))
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp),
            ) {
                Text(
                    stringResource(R.string.ui_settings_option_saveFolder_batchesFolderInaccessible_error),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    )
}

@Composable
fun _CustomFolderDialog(
    onAbort: () -> Unit,
    onOk: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onAbort,
        icon = {
            Icon(
                Icons.Default.Folder,
                contentDescription = null,
            )
        },
        title = {
            Text(stringResource(R.string.ui_welcome_saveFolder_customFolder_title))
        },
        text = {
            Text(stringResource(R.string.ui_welcome_saveFolder_customFolder_message))
        },
        dismissButton = {
            TextButton(
                onClick = onAbort,
                contentPadding = ButtonDefaults.TextButtonContentPadding,
                colors = ButtonDefaults.textButtonColors(),
            ) {
                Text(stringResource(R.string.dialog_close_cancel_label))
            }
        },
        confirmButton = {
            Button(
                onClick = onOk,
            ) {
                Text(stringResource(R.string.dialog_close_neutral_label))
            }
        }
    )

}