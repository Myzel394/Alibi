package app.myzel394.alibi.ui.components.SettingsScreen.Tiles

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
import app.myzel394.alibi.R
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.ui.VIDEO_RECORDER_SUPPORTS_CUSTOM_FOLDER
import app.myzel394.alibi.ui.components.atoms.SettingsTile
import app.myzel394.alibi.ui.utils.rememberFolderSelectorDialog
import kotlinx.coroutines.launch
import java.net.URLDecoder

@Composable
fun SaveFolderTile(
    settings: AppSettings,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val dataStore = context.dataStore

    fun updateValue(path: String?) {
        if (settings.saveFolder != null) {
            runCatching {
                context.contentResolver.releasePersistableUriPermission(
                    Uri.parse(settings.saveFolder),
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            }
        }

        if (path != null) {
            context.contentResolver.takePersistableUriPermission(
                Uri.parse(path),
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        }

        scope.launch {
            dataStore.updateData {
                it.setSaveFolder(path)
            }
        }
    }

    val selectFolder = rememberFolderSelectorDialog { folder ->
        if (folder == null) {
            return@rememberFolderSelectorDialog
        }

        updateValue(folder.toString())
    }

    var showWarning by remember { mutableStateOf(false) }

    if (showWarning) {
        val title = stringResource(R.string.ui_settings_option_saveFolder_warning_title)
        val text = stringResource(R.string.ui_settings_option_saveFolder_warning_text)

        AlertDialog(
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                )
            },
            onDismissRequest = {
                showWarning = false
            },
            title = {
                Text(text = title)
            },
            text = {
                Text(text = text)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showWarning = false
                        selectFolder()
                    },
                ) {
                    Text(
                        text = stringResource(R.string.ui_settings_option_saveFolder_warning_action_confirm),
                    )
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showWarning = false
                    },
                    colors = ButtonDefaults.textButtonColors(),
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

    SettingsTile(
        title = stringResource(R.string.ui_settings_option_saveFolder_title),
        description = stringResource(R.string.ui_settings_option_saveFolder_explanation),
        leading = {
            Icon(
                Icons.Default.AudioFile,
                contentDescription = null,
            )
        },
        trailing = {
            Button(
                onClick = {
                    showWarning = true
                },
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
                shape = MaterialTheme.shapes.medium,
            ) {
                Icon(
                    Icons.Default.Folder,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                )
                Spacer(
                    modifier = Modifier.size(ButtonDefaults.IconSpacing)
                )
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
                if (settings.saveFolder != null) {
                    Text(
                        text = stringResource(
                            R.string.form_value_selected,
                            splitPath(settings.saveFolder).joinToString(" > ")
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(
                        colors = ButtonDefaults.filledTonalButtonColors(),
                        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                        onClick = {
                            updateValue(null)
                        }
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize),
                        )
                        Spacer(
                            modifier = Modifier.size(ButtonDefaults.IconSpacing)
                        )
                        Text(
                            text = stringResource(R.string.ui_settings_option_saveFolder_action_default_label),
                        )
                    }
                } else {
                    Text(
                        text = stringResource(
                            R.string.form_value_selected,
                            stringResource(R.string.ui_settings_option_saveFolder_defaultValue)
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                if (!VIDEO_RECORDER_SUPPORTS_CUSTOM_FOLDER) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp),
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color.Yellow,
                            modifier = Modifier.size(ButtonDefaults.IconSize),
                        )
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(
                                stringResource(R.string.ui_settings_option_saveFolder_videoUnsupported),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Yellow,
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
    )
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
