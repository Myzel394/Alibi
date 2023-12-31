package app.myzel394.alibi.ui.components.RecorderScreen.molecules

import android.Manifest
import androidx.camera.core.CameraSelector
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.lifecycle.viewmodel.compose.viewModel
import app.myzel394.alibi.R
import app.myzel394.alibi.ui.BIG_PRIMARY_BUTTON_SIZE
import app.myzel394.alibi.ui.SHEET_BOTTOM_OFFSET
import app.myzel394.alibi.ui.components.RecorderScreen.atoms.CameraPreview
import app.myzel394.alibi.ui.components.atoms.GlobalSwitch
import app.myzel394.alibi.ui.components.atoms.PermissionRequester
import app.myzel394.alibi.ui.effects.rememberPrevious
import app.myzel394.alibi.ui.models.VideoRecorderModel
import app.myzel394.alibi.ui.utils.CameraInfo
import kotlin.math.abs

@OptIn(
    ExperimentalMaterial3Api::class,
)
@Composable
fun VideoRecorderPreparationSheet(
    showPreview: Boolean,
    videoSettings: VideoRecorderModel,
    onDismiss: () -> Unit,
    onPreviewVisible: () -> Unit,
    onPreviewHidden: () -> Unit,
    onStartRecording: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(true)

    val context = LocalContext.current
    val cameras = CameraInfo.queryAvailableCameras(context)

    LaunchedEffect(Unit) {
        videoSettings.init(context)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = {
            if (showPreview)
                Unit
            else
                BottomSheetDefaults.DragHandle()
        },
    ) {
        Box(
            modifier = Modifier
                .pointerInput(Unit) {
                    awaitEachGesture {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (!event.changes.elementAt(0).pressed) {
                                onPreviewHidden()
                                break
                            }
                        }
                    }
                }
        ) {
            if (showPreview) {
                CameraPreview(
                    modifier = Modifier
                        .fillMaxSize(),
                    cameraSelector = videoSettings.cameraSelector,
                )
            } else
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = SHEET_BOTTOM_OFFSET, top = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(30.dp),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier
                                .size(80.dp),
                        )
                        Text(
                            stringResource(R.string.ui_videoRecorder_action_start_settings_label),
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                    PermissionRequester(
                        permission = Manifest.permission.RECORD_AUDIO,
                        icon = Icons.Default.Mic,
                        onPermissionAvailable = {
                            videoSettings.enableAudio = !videoSettings.enableAudio
                        },
                    ) { trigger ->
                        GlobalSwitch(
                            label = stringResource(R.string.ui_videoRecorder_action_start_settings_enableAudio_label),
                            checked = videoSettings.enableAudio,
                            onCheckedChange = {
                                trigger()
                            }
                        )
                    }

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            stringResource(R.string.ui_videoRecorder_action_start_settings_cameraLens_selection_label),
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                        CamerasSelection(
                            cameras = cameras,
                            videoSettings = videoSettings,
                        )
                    }

                    val label =
                        stringResource(R.string.ui_videoRecorder_action_start_settings_start_label)

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        PermissionRequester(
                            permission = Manifest.permission.CAMERA,
                            icon = Icons.Default.CameraAlt,
                            onPermissionAvailable = {
                                onStartRecording()
                            }
                        ) { trigger ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(BIG_PRIMARY_BUTTON_SIZE)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .padding(16.dp)
                                    .semantics {
                                        contentDescription = label
                                    }
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onLongPress = {
                                                onPreviewVisible()
                                            },
                                            onTap = {
                                                trigger()
                                            }
                                        )
                                    },
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )
                            }
                        }
                        Text(
                            stringResource(
                                R.string.ui_videoRecorder_action_preview_label
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
        }
    }
}