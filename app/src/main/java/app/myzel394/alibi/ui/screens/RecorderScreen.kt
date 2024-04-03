package app.myzel394.alibi.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.myzel394.alibi.R
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.db.RecordingInformation
import app.myzel394.alibi.ui.components.RecorderScreen.organisms.AudioRecordingStatus
import app.myzel394.alibi.ui.components.RecorderScreen.organisms.RecorderEventsHandler
import app.myzel394.alibi.ui.components.RecorderScreen.organisms.StartRecording
import app.myzel394.alibi.ui.components.RecorderScreen.organisms.VideoRecordingStatus
import app.myzel394.alibi.ui.models.AudioRecorderModel
import app.myzel394.alibi.ui.models.VideoRecorderModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecorderScreen(
    onNavigateToSettingsScreen: () -> Unit,
    audioRecorder: AudioRecorderModel,
    videoRecorder: VideoRecorderModel,
    settings: AppSettings,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    RecorderEventsHandler(
        settings = settings,
        snackbarHostState = snackbarHostState,
        audioRecorder = audioRecorder,
        videoRecorder = videoRecorder,
    )

    // TopAppBar and AudioRecordingStart should be hidden when
    // the video preview is visible.
    // We need to preview the video inline to
    // be able to capture the touch release event.
    var topBarVisible by remember { mutableStateOf(true) }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = {
                    Snackbar(
                        snackbarData = it,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        actionColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        actionContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        dismissActionContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            )
        },
        topBar = {
            if (topBarVisible)
                return@Scaffold TopAppBar(
                    title = {
                        Text(stringResource(R.string.app_name))
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                onNavigateToSettingsScreen()
                            },
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = null
                            )
                        }
                    }
                )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (audioRecorder.isInRecording)
                AudioRecordingStatus(audioRecorder = audioRecorder)
            else if (videoRecorder.isInRecording)
                VideoRecordingStatus(videoRecorder = videoRecorder)
            else
                StartRecording(
                    audioRecorder = audioRecorder,
                    videoRecorder = videoRecorder,
                    appSettings = settings,
                    onSaveLastRecording = {
                        scope.launch {
                            when (settings.lastRecording!!.type) {
                                RecordingInformation.Type.AUDIO ->
                                    audioRecorder.onRecordingSave(false)

                                RecordingInformation.Type.VIDEO ->
                                    videoRecorder.onRecordingSave(false)
                            }
                        }
                    },
                    showAudioRecorder = topBarVisible,
                    onHideTopBar = {
                        topBarVisible = false
                    },
                    onShowTopBar = {
                        topBarVisible = true
                    },
                )
        }
    }
}
