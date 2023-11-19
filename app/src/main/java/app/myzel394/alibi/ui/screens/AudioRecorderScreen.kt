package app.myzel394.alibi.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavController
import app.myzel394.alibi.ui.components.AudioRecorder.organisms.RecordingStatus
import app.myzel394.alibi.ui.components.AudioRecorder.molecules.StartRecording
import app.myzel394.alibi.ui.enums.Screen
import app.myzel394.alibi.ui.utils.rememberFileSaverDialog
import app.myzel394.alibi.R
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.helpers.AudioRecorderExporter
import app.myzel394.alibi.helpers.BatchesFolder
import app.myzel394.alibi.ui.effects.rememberSettings
import app.myzel394.alibi.ui.models.AudioRecorderModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioRecorderScreen(
    navController: NavController,
    audioRecorder: AudioRecorderModel,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val dataStore = context.dataStore
    val settings = rememberSettings()
    val scope = rememberCoroutineScope()

    val saveFile = rememberFileSaverDialog(
        settings.audioRecorderSettings.getMimeType()
    ) {
        if (settings.audioRecorderSettings.deleteRecordingsImmediately) {
            audioRecorder.batchesFolder!!.deleteRecordings()
        }

        if (!audioRecorder.batchesFolder!!.hasRecordingsAvailable()) {
            scope.launch {
                dataStore.updateData {
                    it.setLastRecording(null)
                }
            }
        }
    }

    var isProcessingAudio by remember { mutableStateOf(false) }
    var showRecorderError by remember { mutableStateOf(false) }

    fun saveAsLastRecording() {
        if (!settings.audioRecorderSettings.deleteRecordingsImmediately) {
            scope.launch {
                dataStore.updateData {
                    it.setLastRecording(
                        audioRecorder.recorderService!!.getRecordingInformation()
                    )
                }
            }
        }
    }

    val successMessage = stringResource(R.string.ui_audioRecorder_action_save_success)
    val openMessage = stringResource(R.string.ui_audioRecorder_action_save_openFolder)

    fun openFolder(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW, uri)

        context.startActivity(intent)
    }

    fun showSnackbar(uri: Uri) {
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = successMessage,
                actionLabel = openMessage,
                duration = SnackbarDuration.Short,
            )

            if (result == SnackbarResult.ActionPerformed) {
                openFolder(uri)
            }
        }
    }

    fun saveRecording() {
        scope.launch {
            isProcessingAudio = true

            // Give the user some time to see the processing dialog
            delay(100)

            try {
                val recording = audioRecorder.recorderService?.getRecordingInformation()
                    ?: settings.lastRecording
                    ?: throw Exception("No recording information available")
                val outputFile = audioRecorder.batchesFolder!!.getOutputFileForFFmpeg(
                    recording.recordingStart,
                    recording.fileExtension
                )

                AudioRecorderExporter(recording).concatenateFiles(
                    audioRecorder.recorderService!!.batchesFolder,
                    outputFile,
                )

                val name = audioRecorder.batchesFolder!!.getName(
                    recording.recordingStart,
                    recording.fileExtension,
                )

                when (audioRecorder.batchesFolder!!.type) {
                    BatchesFolder.BatchType.INTERNAL -> {
                        saveFile(
                            audioRecorder.batchesFolder!!.asInternalGetOutputFile(
                                recording.recordingStart,
                                recording.fileExtension,
                            ), name
                        )
                    }

                    BatchesFolder.BatchType.CUSTOM -> {
                        showSnackbar(audioRecorder.batchesFolder!!.customFolder!!.uri)

                        if (settings.audioRecorderSettings.deleteRecordingsImmediately) {
                            audioRecorder.batchesFolder!!.deleteRecordings()
                        }
                    }
                }
            } catch (error: Exception) {
                Log.getStackTraceString(error)
            } finally {
                isProcessingAudio = false
            }
        }
    }

    DisposableEffect(key1 = audioRecorder, key2 = settings) {
        audioRecorder.onRecordingSave = onRecordingSave@{
            saveAsLastRecording()

            saveRecording()
        }
        audioRecorder.onError = {
            saveAsLastRecording()

            audioRecorder.stopRecording(context)
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
                        showRecorderError = false

                        saveRecording()
                    },
                    colors = ButtonDefaults.textButtonColors(),
                ) {
                    Text(stringResource(R.string.ui_audioRecorder_action_save_label))
                }
            }
        )
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
            val appSettings =
                context.dataStore.data.collectAsState(AppSettings.getDefaultInstance()).value

            if (audioRecorder.isInRecording)
                RecordingStatus(audioRecorder = audioRecorder)
            else
                StartRecording(
                    audioRecorder = audioRecorder, appSettings = appSettings,
                    onSaveLastRecording = ::saveRecording,
                )
        }
    }
}
