package app.myzel394.alibi.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import app.myzel394.alibi.R
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.helpers.Doctor

// Handlers that can safely be run when the app is locked (biometric authentication required)
@Composable
fun LockedAppHandlers() {
    val context = LocalContext.current
    val settings = context
        .dataStore
        .data
        .collectAsState(initial = null)
        .value ?: return

    LaunchedEffect(settings.theme) {
        if (!SUPPORTS_DARK_MODE_NATIVELY) {
            val currentValue = AppCompatDelegate.getDefaultNightMode()

            if (settings.theme == AppSettings.Theme.LIGHT && currentValue != AppCompatDelegate.MODE_NIGHT_NO) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else if (settings.theme == AppSettings.Theme.DARK && currentValue != AppCompatDelegate.MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
    }

    var showFileSaverUnavailableDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val doctor = Doctor(context)

        if (!doctor.checkIfFileSaverDialogIsAvailable()) {
            showFileSaverUnavailableDialog = true
        }
    }

    if (showFileSaverUnavailableDialog) {
        AlertDialog(
            icon = {
                Icon(
                    Icons.Default.Error,
                    contentDescription = null
                )
            },
            onDismissRequest = {
                showFileSaverUnavailableDialog = false
            },
            title = {
                Text(stringResource(R.string.ui_severeError_fileSaverUnavailable_title))
            },
            text = {
                Text(stringResource(R.string.ui_severeError_fileSaverUnavailable_text))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showFileSaverUnavailableDialog = false
                    }
                ) {
                    Text(text = stringResource(R.string.dialog_close_neutral_label))
                }
            }
        )
    }
}