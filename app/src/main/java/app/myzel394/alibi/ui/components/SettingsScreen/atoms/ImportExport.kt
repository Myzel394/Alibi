package app.myzel394.alibi.ui.components.SettingsScreen.atoms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import app.myzel394.alibi.R
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.ui.utils.rememberFileSaverDialog
import app.myzel394.alibi.ui.utils.rememberFileSelectorDialog
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun ImportExport(
    snackbarHostState: SnackbarHostState,
) {
    val context = LocalContext.current

    val scope = rememberCoroutineScope()
    val dataStore = LocalContext.current.dataStore
    val settings = dataStore
        .data
        .collectAsState(initial = AppSettings.getDefaultInstance())
        .value

    var settingsToBeImported by remember { mutableStateOf<AppSettings?>(null) }

    val saveFile = rememberFileSaverDialog("application/json")
    val openFile = rememberFileSelectorDialog { uri ->
        val file = File.createTempFile("alibi_settings", ".json")

        context.contentResolver.openInputStream(uri)!!.use {
            it.copyTo(file.outputStream())
        }
        val rawContent = file.readText()

        settingsToBeImported = AppSettings.fromExportedString(rawContent)
    }

    if (settingsToBeImported != null) {
        val successMessage = stringResource(R.string.ui_settings_option_import_success)

        AlertDialog(
            onDismissRequest = {
                settingsToBeImported = null
            },
            title = {
                Text(stringResource(R.string.ui_settings_option_import_label))
            },
            text = {
                Text(stringResource(R.string.ui_settings_option_import_dialog_text))
            },
            icon = {
                Icon(
                    Icons.Default.Download,
                    contentDescription = null,
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            dataStore.updateData {
                                settingsToBeImported!!
                            }
                            settingsToBeImported = null

                            snackbarHostState.showSnackbar(
                                message = successMessage,
                                withDismissAction = true,
                                duration = SnackbarDuration.Short,
                            )
                        }

                    },
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                    )
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text(stringResource(R.string.ui_settings_option_import_dialog_confirm))
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        settingsToBeImported = null
                    },
                    colors = ButtonDefaults.textButtonColors(),
                ) {
                    Text(stringResource(R.string.dialog_close_cancel_label))
                }
            },
        )
    }

    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Button(
            onClick = {
                openFile("application/json")
            },
            colors = ButtonDefaults.filledTonalButtonColors(),
        ) {
            Icon(
                Icons.Default.Download,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize),
            )
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.ui_settings_option_import_label))
        }
        Button(
            onClick = {
                val rawContent = settings.exportToString()

                val tempFile = File.createTempFile("alibi_settings", ".json")
                tempFile.writeText(rawContent)

                saveFile(tempFile, "alibi_settings.json")
            },
            colors = ButtonDefaults.filledTonalButtonColors(),
        ) {
            Icon(
                Icons.Default.Upload,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize),
            )
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(R.string.ui_settings_option_export_label))
        }
    }
}