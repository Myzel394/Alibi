package app.myzel394.alibi.ui.components.RecorderScreen.atoms

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import app.myzel394.alibi.R
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.db.RecordingInformation
import app.myzel394.alibi.helpers.AudioBatchesFolder
import app.myzel394.alibi.helpers.BatchesFolder
import app.myzel394.alibi.services.IntervalRecorderService
import app.myzel394.alibi.ui.models.AudioRecorderModel
import app.myzel394.alibi.ui.models.BaseRecorderModel
import app.myzel394.alibi.ui.models.VideoRecorderModel
import app.myzel394.alibi.ui.utils.rememberFileSaverDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

typealias RecorderModel = BaseRecorderModel<
        IntervalRecorderService.Settings,
        RecordingInformation,
        IntervalRecorderService<IntervalRecorderService.Settings, RecordingInformation>,
        BatchesFolder?
        >

@Composable
fun RecorderEventsHandler(
    settings: AppSettings,
    snackbarHostState: SnackbarHostState,
    audioRecorder: AudioRecorderModel,
    videoRecorder: VideoRecorderModel,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val dataStore = context.dataStore

    var isProcessing by remember { mutableStateOf(false) }
    var showRecorderError by remember { mutableStateOf(false) }

    val saveFile = rememberFileSaverDialog(
        settings.audioRecorderSettings.getMimeType()
    ) {
        if (settings.deleteRecordingsImmediately) {
            runCatching {
                audioRecorder.batchesFolder?.deleteRecordings()
            }
            runCatching {
                videoRecorder.batchesFolder?.deleteRecordings()
            }
        }

        if (audioRecorder.batchesFolder?.hasRecordingsAvailable() == false
            && videoRecorder.batchesFolder?.hasRecordingsAvailable() == false
        ) {
            scope.launch {
                dataStore.updateData {
                    it.setLastRecording(null)
                }
            }
        }
    }

    fun saveAsLastRecording(
        recorder: RecorderModel
    ) {
        if (!settings.deleteRecordingsImmediately) {
            scope.launch {
                dataStore.updateData {
                    it.setLastRecording(
                        recorder.recorderService!!.getRecordingInformation()
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

    fun saveRecording(recorder: RecorderModel) {
        scope.launch {
            isProcessing = true

            // Give the user some time to see the processing dialog
            delay(100)

            try {

                val recording =
                    // When new recording created
                    recorder.recorderService?.getRecordingInformation()
                    // When recording is loaded from lastRecording
                        ?: settings.lastRecording
                        ?: throw Exception("No recording information available")
                val batchesFolder =
                    AudioBatchesFolder.importFromFolder(recording.folderPath, context)

                batchesFolder.concatenate(
                    recording.recordingStart,
                    recording.fileExtension,
                )

                // Save file
                val name = batchesFolder.getName(
                    recording.recordingStart,
                    recording.fileExtension,
                )

                when (batchesFolder.type) {
                    BatchesFolder.BatchType.INTERNAL -> {
                        saveFile(
                            batchesFolder.asInternalGetOutputFile(
                                recording.recordingStart,
                                recording.fileExtension,
                            ), name
                        )
                    }

                    BatchesFolder.BatchType.CUSTOM -> {
                        showSnackbar(batchesFolder.customFolder!!.uri)

                        if (settings.deleteRecordingsImmediately) {
                            batchesFolder.deleteRecordings()
                        }
                    }
                }
            } catch (error: Exception) {
                Log.getStackTraceString(error)
            } finally {
                isProcessing = false
            }
        }
    }

    // Register audio recorder events
    DisposableEffect(key1 = audioRecorder, key2 = settings) {
        audioRecorder.onRecordingSave = {
            saveAsLastRecording(audioRecorder as RecorderModel)

            saveRecording(audioRecorder)
        }
        audioRecorder.onError = {
            saveAsLastRecording(audioRecorder as RecorderModel)

            showRecorderError = true
        }

        onDispose {
            audioRecorder.onRecordingSave = {}
            audioRecorder.onError = {}
        }
    }

    // Register video recorder events
    DisposableEffect(key1 = videoRecorder, key2 = settings) {
        videoRecorder.onRecordingSave = {
            saveAsLastRecording(videoRecorder as RecorderModel)

            saveRecording(videoRecorder)
        }
        videoRecorder.onError = {
            saveAsLastRecording(videoRecorder as RecorderModel)

            showRecorderError = true
        }

        onDispose {
            videoRecorder.onRecordingSave = {}
            videoRecorder.onError = {}
        }
    }

    if (isProcessing)
        RecorderProcessingDialog()

    if (showRecorderError)
        RecorderErrorDialog(
            onClose = {
                showRecorderError = false
            },
            onSave = {
            },
        )
}