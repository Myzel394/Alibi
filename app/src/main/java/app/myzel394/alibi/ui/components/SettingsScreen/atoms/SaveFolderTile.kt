package app.myzel394.alibi.ui.components.SettingsScreen.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toFile
import app.myzel394.alibi.R
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.helpers.AudioRecorderExporter
import app.myzel394.alibi.ui.components.atoms.SettingsTile
import app.myzel394.alibi.ui.utils.rememberFolderSelectorDialog
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun SaveFolderTile(
    settings: AppSettings,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val dataStore = context.dataStore

    fun updateValue(path: String?) {
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

        updateValue(folder.path)
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
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (settings.audioRecorderSettings.saveFolder != null) {
                    Button(
                        colors = ButtonDefaults.filledTonalButtonColors(),
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
                }
                Text(
                    text = stringResource(
                        R.string.form_value_selected,
                        settings.audioRecorderSettings.saveFolder
                            ?: stringResource(R.string.ui_settings_option_saveFolder_defaultValue)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    )
}
