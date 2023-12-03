package app.myzel394.alibi.ui.components.AudioRecorder.molecules

import android.graphics.Point
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
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
import app.myzel394.alibi.ui.components.AudioRecorder.atoms.CameraPreview
import app.myzel394.alibi.ui.components.atoms.GlobalSwitch
import app.myzel394.alibi.ui.models.VideoRecorderSettingsModel
import app.myzel394.alibi.ui.utils.CameraInfo

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun VideoRecorderPreparationSheet(
    onDismiss: () -> Unit,
    videoSettings: VideoRecorderSettingsModel = viewModel(),
    onPreviewVisible: () -> Unit,
    onPreviewHidden: () -> Unit,
    showPreview: Boolean,
) {
    val sheetState = rememberModalBottomSheetState(true)

    val context = LocalContext.current
    val cameras = CameraInfo.queryAvailableCameras(context)

    if (showPreview)
        CameraPreview(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        )
    else {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
        ) {
            Box(
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 24.dp),
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
                    GlobalSwitch(
                        label = stringResource(R.string.ui_videoRecorder_action_start_settings_enableAudio_label),
                        checked = videoSettings.enableAudio,
                        onCheckedChange = {
                            videoSettings.enableAudio = it
                        }
                    )

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

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(BIG_PRIMARY_BUTTON_SIZE)
                    )
                }

            }
        }
    }

    Popup(
        alignment = Alignment.BottomCenter,
    ) {
        val label =
            stringResource(R.string.ui_videoRecorder_action_start_settings_start_label)

        Box(
            modifier = Modifier
                .pointerInput(Unit) {
                    awaitEachGesture {
                        while (true) {
                            val event = awaitPointerEvent()
                            // consume all changes

                            if (!event.changes.elementAt(0).pressed) {
                                onPreviewHidden()
                                break
                            }
                        }
                    }
                }
                .let {
                    if (showPreview) it.alpha(0.2f) else it
                }
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
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
    }
}