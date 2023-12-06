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
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import app.myzel394.alibi.ui.components.RecorderScreen.organisms.AudioRecordingStatus
import app.myzel394.alibi.ui.components.RecorderScreen.organisms.StartRecording
import app.myzel394.alibi.ui.enums.Screen
import app.myzel394.alibi.R
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.ui.components.RecorderScreen.atoms.RecorderEventsHandler
import app.myzel394.alibi.ui.components.RecorderScreen.organisms.VideoRecordingStatus
import app.myzel394.alibi.ui.effects.rememberSettings
import app.myzel394.alibi.ui.models.AudioRecorderModel
import app.myzel394.alibi.ui.models.VideoRecorderModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecorderScreen(
    navController: NavController,
    audioRecorder: AudioRecorderModel,
    videoRecorder: VideoRecorderModel,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val settings = rememberSettings()

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
                                navController.navigate(Screen.Settings.route)
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
            val appSettings =
                context.dataStore.data.collectAsState(AppSettings.getDefaultInstance()).value

            if (audioRecorder.isInRecording)
                AudioRecordingStatus(audioRecorder = audioRecorder)
            else if (videoRecorder.isInRecording)
                VideoRecordingStatus(videoRecorder = videoRecorder)
            else
                StartRecording(
                    audioRecorder = audioRecorder,
                    videoRecorder = videoRecorder,
                    appSettings = appSettings,
                    onSaveLastRecording = {
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
