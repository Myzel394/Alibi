package app.myzel394.alibi.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import app.myzel394.alibi.ui.components.AudioRecorder.molecules.RecordingStatus
import app.myzel394.alibi.ui.components.AudioRecorder.molecules.StartRecording
import app.myzel394.alibi.ui.enums.Screen
import app.myzel394.alibi.ui.utils.rememberFileSaverDialog
import app.myzel394.alibi.R
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.db.LastRecording
import app.myzel394.alibi.ui.effects.rememberSettings
import app.myzel394.alibi.ui.models.AudioRecorderModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioRecorder(
    navController: NavController,
    audioRecorder: AudioRecorderModel,
) {
    val context = LocalContext.current
    val settings = rememberSettings()
    val saveFile = rememberFileSaverDialog(settings.audioRecorderSettings.getMimeType())
    val scope = rememberCoroutineScope()

    var isProcessingAudio by remember { mutableStateOf(false) }
    var showRecorderError by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        audioRecorder.onRecordingSave = {
            scope.launch {
                isProcessingAudio = true

                try {
                    val file = audioRecorder.lastRecording!!.concatenateFiles()

                    saveFile(file, file.name)
                } catch (error: Exception) {
                    Log.getStackTraceString(error)
                } finally {
                    isProcessingAudio = false
                }
            }
        }
        audioRecorder.onError = {
            // No need to save last recording as it's done automatically on error
            audioRecorder.stopRecording(context, saveAsLastRecording = false)
            showRecorderError = true
        }

        onDispose {
            audioRecorder.onRecordingSave = {}
            audioRecorder.onError = {}
        }
    }

    if (isProcessingAudio)
        AlertDialog(
            onDismissRequest = { },
            icon = {
                Icon(
                    Icons.Default.Memory,
                    contentDescription = null,
                )
            },
            title = {
                Text(
                    stringResource(R.string.ui_audioRecorder_action_save_processing_dialog_title),
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        stringResource(R.string.ui_audioRecorder_action_save_processing_dialog_description),
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    LinearProgressIndicator()
                }
            },
            confirmButton = {}
        )
    if (showRecorderError)
        AlertDialog(
            onDismissRequest = { showRecorderError = false },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                )
            },
            title = {
                Text(stringResource(R.string.ui_audioRecorder_error_recording_title))
            },
            text = {
                Text(stringResource(R.string.ui_audioRecorder_error_recording_description))
            },
            dismissButton = {
                Button(
                    onClick = { showRecorderError = false },
                    colors = ButtonDefaults.textButtonColors(),
                ) {
                    Text(stringResource(R.string.dialog_close_cancel_label))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        audioRecorder.onRecordingSave()
                    },
                    colors = ButtonDefaults.textButtonColors(),
                ) {
                    Text(stringResource(R.string.ui_audioRecorder_action_save_label))
                }
            }
        )
    Scaffold(
        topBar = {
            TopAppBar(
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
            if (audioRecorder.isInRecording)
                RecordingStatus(audioRecorder = audioRecorder)
            else
                StartRecording(audioRecorder = audioRecorder)
        }
    }
}