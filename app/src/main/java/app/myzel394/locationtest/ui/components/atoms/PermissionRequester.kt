package app.myzel394.locationtest.ui.components.atoms

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.myzel394.locationtest.R
import app.myzel394.locationtest.ui.utils.PermissionHelper
import app.myzel394.locationtest.ui.utils.openAppSystemSettings

@Composable
fun PermissionRequester(
    permission: Array<String>,
    icon: (@Composable () -> Unit)? = null,
    onPermissionAvailable: () -> Unit,
    content: @Composable (trigger: () -> Unit) -> Unit
) {
    val context = LocalContext.current

    var showExplanationDialog by remember { mutableStateOf(false) }

    fun callback() {
        if (PermissionHelper.hasPermanentlyDenied(context, permission)) {
            showExplanationDialog = true
            return
        }

        if (PermissionHelper.checkPermissions(context, permission)) {
            onPermissionAvailable()
        }
    }

    if (showExplanationDialog) {
        AlertDialog(
            onDismissRequest = {
                showExplanationDialog = false
            },
            icon = icon,
            title = {
                Text(stringResource(R.string.ui_permissions_request_title))
            },
            text = {
                Text(stringResource(R.string.ui_permissions_request_message))
            },
            confirmButton = {
                Button(
                    onClick = {
                        showExplanationDialog = false
                        context.openAppSystemSettings()
                    }
                ) {
                    Icon(
                        Icons.Default.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                    )
                    Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                    Text(stringResource(R.string.dialog_close_neutral_label))
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showExplanationDialog = false
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
    content(::callback)
}