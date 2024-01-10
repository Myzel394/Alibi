package app.myzel394.alibi.ui.components.RecorderScreen.molecules

import android.Manifest
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.myzel394.alibi.R
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.ui.BIG_PRIMARY_BUTTON_SIZE
import app.myzel394.alibi.ui.SUPPORTS_SCOPED_STORAGE
import app.myzel394.alibi.ui.components.RecorderScreen.atoms.BigButton
import app.myzel394.alibi.ui.components.atoms.PermissionRequester
import app.myzel394.alibi.ui.models.VideoRecorderModel
import app.myzel394.alibi.ui.utils.PermissionHelper

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoRecordingStart(
    videoRecorder: VideoRecorderModel,
    appSettings: AppSettings,
    onHideAudioRecording: () -> Unit,
    onShowAudioRecording: () -> Unit,
    showPreview: Boolean,
) {
    val context = LocalContext.current

    var showSheet by rememberSaveable {
        mutableStateOf(false)
    }

    if (showSheet) {
        VideoRecorderPreparationSheet(
            showPreview = showPreview,
            videoSettings = videoRecorder,
            onDismiss = {
                showSheet = false
            },
            onPreviewVisible = onHideAudioRecording,
            onPreviewHidden = onShowAudioRecording,
            onStartRecording = {
                videoRecorder.startRecording(context, appSettings)
            },
        )
    }

    PermissionRequester(
        permission = Manifest.permission.WRITE_EXTERNAL_STORAGE,
        icon = Icons.Default.InsertDriveFile,
        onPermissionAvailable = {
            showSheet = true
        }
    ) { triggerExternalStorage ->
        BigButton(
            label = stringResource(R.string.ui_videoRecorder_action_start_label),
            description = stringResource(R.string.ui_videoRecorder_action_configure_label),
            icon = Icons.Default.CameraAlt,
            onLongClick = {
                if (appSettings.requiresExternalStoragePermission(context)) {
                    triggerExternalStorage()
                    return@BigButton
                }

                showSheet = true
            },
            onClick = {
                if (appSettings.requiresExternalStoragePermission(context)) {
                    triggerExternalStorage()
                    return@BigButton
                }

                if (PermissionHelper.hasGranted(
                        context,
                        Manifest.permission.CAMERA
                    ) && PermissionHelper.hasGranted(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    )
                ) {
                    videoRecorder.startRecording(context, appSettings)
                } else {
                    showSheet = true
                }
            },
        )
    }
}