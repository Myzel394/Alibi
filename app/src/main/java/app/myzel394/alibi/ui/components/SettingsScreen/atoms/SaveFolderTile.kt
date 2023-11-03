package app.myzel394.alibi.ui.components.SettingsScreen.atoms

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.myzel394.alibi.R
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.ui.components.atoms.SettingsTile
import app.myzel394.alibi.ui.utils.rememberFolderSelectorDialog
import kotlinx.coroutines.launch

@Composable
fun SaveFolderTile(
    settings: AppSettings,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val dataStore = context.dataStore

    fun updateValue(path: String?) {
        if (settings.audioRecorderSettings.saveFolder != null) {
            runCatching {
                context.contentResolver.releasePersistableUriPermission(
                    Uri.parse(settings.audioRecorderSettings.saveFolder),
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            }
        }

        scope.launch {
            dataStore.updateData {
                it.setAudioRecorderSettings(
                    it.audioRecorderSettings.setSaveFolder(path)
                )
            }
        }
    }

    val selectFolder = rememberFolderSelectorDialog { folder ->
        if (folder == null) {
            return@rememberFolderSelectorDialog
        }

        context.contentResolver.takePersistableUriPermission(
            folder,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        )

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
                if (settings.audioRecorderSettings.saveFolder != null) {
                    Text(
                        text = stringResource(
                            R.string.form_value_selected,
                            settings
                                .audioRecorderSettings
                                .saveFolder
                                .split(":")[1]
                                .replace("/", " > ")
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
            }
        }
    )
}
