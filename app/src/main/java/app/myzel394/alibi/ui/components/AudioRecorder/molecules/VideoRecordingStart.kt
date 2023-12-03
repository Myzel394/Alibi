package app.myzel394.alibi.ui.components.AudioRecorder.molecules

import android.Manifest
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.myzel394.alibi.R
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.ui.components.atoms.PermissionRequester
import app.myzel394.alibi.ui.models.VideoRecorderModel

@Composable
fun VideoRecordingStart(
    videoRecorder: VideoRecorderModel,
    appSettings: AppSettings,
) {
    val context = LocalContext.current

    var showSheet by rememberSaveable {
        mutableStateOf(false)
    }

    if (showSheet) {
        VideoRecorderPreparationSheet(
            onDismiss = {
                showSheet = false
            },
        )
    }

    PermissionRequester(
        permission = Manifest.permission.RECORD_AUDIO,
        icon = Icons.Default.Mic,
        onPermissionAvailable = {
            showSheet = true
        },
    ) { trigger ->
        val label = stringResource(R.string.ui_videoRecorder_action_start_label)

        Button(
            onClick = trigger,
            modifier = Modifier
                .semantics {
                    contentDescription = label
                }
                .size(200.dp)
                .clip(shape = CircleShape),
            colors = ButtonDefaults.outlinedButtonColors(),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp),
                )
                Spacer(modifier = Modifier.height(ButtonDefaults.IconSpacing))
                Text(
                    label,
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        }
    }
}