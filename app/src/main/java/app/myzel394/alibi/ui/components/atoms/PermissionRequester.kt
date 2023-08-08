package app.myzel394.alibi.ui.components.atoms

import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import app.myzel394.alibi.R
import app.myzel394.alibi.ui.utils.PermissionHelper
import app.myzel394.alibi.ui.utils.openAppSystemSettings

@Composable
fun PermissionRequester(
    permission: String,
    icon: ImageVector,
    onPermissionAvailable: () -> Unit,
    content: @Composable (trigger: () -> Unit) -> Unit
) {
    val context = LocalContext.current

    var isGranted by remember { mutableStateOf(PermissionHelper.hasGranted(context, permission)) }
    var visibleDialog by remember { mutableStateOf<VisibleDialog?>(null) }
    val requestPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isPermissionGranted: Boolean ->
            isGranted = isPermissionGranted

            if (isGranted) {
                onPermissionAvailable()
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, permission)) {
                    visibleDialog = VisibleDialog.REQUEST
                } else {
                    visibleDialog = VisibleDialog.PERMANENTLY_DENIED
                }
            }
        }
    )

    fun callback() {
        if (isGranted) {
            onPermissionAvailable()
        } else {
            requestPermission.launch(permission)
        }
    }

    if (visibleDialog == VisibleDialog.REQUEST) {
        AlertDialog(
            onDismissRequest = {
                visibleDialog = null
            },
            icon = {
                Icon(
                    icon,
                    contentDescription = null,
                )
            },
            title = {
                Text(stringResource(R.string.ui_permissions_request_title))
            },
            text = {
                Text(stringResource(R.string.ui_permissions_request))
            },
            confirmButton = {
                Button(
                    onClick = {
                        visibleDialog = null
                        callback()
                    }
                ) {
                    Icon(
                        Icons.Default.Check,
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
                        visibleDialog = null
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
    if (visibleDialog == VisibleDialog.PERMANENTLY_DENIED) {
        AlertDialog(
            onDismissRequest = {
                visibleDialog = null
            },
            icon = {
                Icon(
                    icon,
                    contentDescription = null,
                )
            },
            title = {
                Text(stringResource(R.string.ui_permissions_request_title))
            },
            text = {
                Column {
                    Text(stringResource(R.string.ui_permissions_request))
                    Spacer(modifier = Modifier.height(32.dp))
                    MessageBox(
                        type = MessageType.INFO,
                        message = stringResource(R.string.ui_permissions_permanentlyDenied_message)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        visibleDialog = null
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
                        visibleDialog = null
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

enum class VisibleDialog {
    REQUEST,
    PERMANENTLY_DENIED
}