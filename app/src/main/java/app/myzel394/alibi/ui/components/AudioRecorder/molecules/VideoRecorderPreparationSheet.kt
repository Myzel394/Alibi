package app.myzel394.alibi.ui.components.AudioRecorder.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.myzel394.alibi.R
import app.myzel394.alibi.ui.BIG_PRIMARY_BUTTON_SIZE
import app.myzel394.alibi.ui.components.atoms.GlobalSwitch
import app.myzel394.alibi.ui.models.VideoRecorderSettingsModel
import app.myzel394.alibi.ui.utils.CameraInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoRecorderPreparationSheet(
    onDismiss: () -> Unit,
    videoSettings: VideoRecorderSettingsModel = viewModel()
) {
    val sheetState = rememberModalBottomSheetState(true)

    val context = LocalContext.current
    val cameras = CameraInfo.queryAvailableCameras(context)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
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

            val label = stringResource(R.string.ui_videoRecorder_action_start_settings_start_label)
            Button(
                onClick = {},
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .height(BIG_PRIMARY_BUTTON_SIZE)
                    .semantics {
                        contentDescription = label
                    }
            ) {
                Text(label)
            }
        }
    }
}