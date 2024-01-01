package app.myzel394.alibi.ui.components.RecorderScreen.organisms

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
import app.myzel394.alibi.helpers.VideoBatchesFolder
import app.myzel394.alibi.services.IntervalRecorderService
import app.myzel394.alibi.ui.components.RecorderScreen.atoms.BatchesInaccessibleDialog
import app.myzel394.alibi.ui.components.RecorderScreen.atoms.RecorderErrorDialog
import app.myzel394.alibi.ui.components.RecorderScreen.atoms.RecorderProcessingDialog
import app.myzel394.alibi.ui.models.AudioRecorderModel
import app.myzel394.alibi.ui.models.BaseRecorderModel
import app.myzel394.alibi.ui.models.VideoRecorderModel
import app.myzel394.alibi.ui.utils.rememberFileSaverDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

typealias RecorderModel = BaseRecorderModel<
        RecordingInformation,
        BatchesFolder,
        IntervalRecorderService<RecordingInformation, BatchesFolder>,
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
    var showBatchesInaccessibleError by remember { mutableStateOf(false) }

    val saveAudioFile = rememberFileSaverDialog(settings.audioRecorderSettings.getMimeType()) {
        if (settings.deleteRecordingsImmediately) {
            runCatching {
                audioRecorder.batchesFolder?.deleteRecordings()
            }
        }

        if (audioRecorder.batchesFolder?.hasRecordingsAvailable() == false) {
            scope.launch {
                dataStore.updateData {
                    it.setLastRecording(null)
                }
            }
        }
    }
    val saveVideoFile = rememberFileSaverDialog(settings.videoRecorderSettings.getMimeType()) {
        if (settings.deleteRecordingsImmediately) {
            runCatching {
                videoRecorder.batchesFolder?.deleteRecordings()
            }
        }

        if (videoRecorder.batchesFolder?.hasRecordingsAvailable() == false) {
            scope.launch {
                dataStore.updateData {
                    it.setLastRecording(null)
                }
            }
        }
    }

    suspend fun saveAsLastRecording(
        recorder: RecorderModel
    ) {
        if (!settings.deleteRecordingsImmediately) {
            dataStore.updateData {
                it.setLastRecording(
                    recorder.recorderService!!.getRecordingInformation()
                )
            }
        }
    }

    val successMessage = stringResource(R.string.ui_audioRecorder_action_save_success)
    val openMessage = stringResource(R.string.ui_audioRecorder_action_save_openFolder)

    fun openFolder(uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW, uri)

        context.startActivity(intent)
    }

    fun showSnackbar() {
        scope.launch {
            snackbarHostState.showSnackbar(
                message = successMessage,
                duration = SnackbarDuration.Short,
            )
        }
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

    suspend fun saveRecording(recorder: RecorderModel) {
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
            val batchesFolder = when (recorder.javaClass) {
                AudioRecorderModel::class.java -> AudioBatchesFolder.importFromFolder(
                    recording.folderPath,
                    context
                )

                VideoRecorderModel::class.java -> VideoBatchesFolder.importFromFolder(
                    recording.folderPath,
                    context
                )

                else -> throw Exception("Unknown recorder type")
            }

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
                    when (batchesFolder) {
                        is AudioBatchesFolder -> {
                            saveAudioFile(
                                batchesFolder.asInternalGetOutputFile(
                                    recording.recordingStart,
                                    recording.fileExtension,
                                ), name
                            )
                        }

                        is VideoBatchesFolder -> {
                            saveVideoFile(
                                batchesFolder.asInternalGetOutputFile(
                                    recording.recordingStart,
                                    recording.fileExtension,
                                ), name
                            )
                        }
                    }
                }

                BatchesFolder.BatchType.CUSTOM -> {
                    showSnackbar(batchesFolder.customFolder!!.uri)

                    if (settings.deleteRecordingsImmediately) {
                        batchesFolder.deleteRecordings()
                    }
                }

                BatchesFolder.BatchType.MEDIA -> {
                    showSnackbar()

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

    // Register audio recorder events
    DisposableEffect(key1 = audioRecorder, key2 = settings) {
        audioRecorder.onRecordingSave = { justSave ->
            scope.launch {
                if (justSave) {
                    saveRecording(audioRecorder as RecorderModel)
                } else {
                    audioRecorder.stopRecording(context)

                    saveAsLastRecording(audioRecorder as RecorderModel)

                    saveRecording(audioRecorder)

                    audioRecorder.destroyService(context)
                }
            }
        }
        audioRecorder.onRecordingStart = {
            snackbarHostState.currentSnackbarData?.dismiss()
        }
        audioRecorder.onError = {
            scope.launch {
                saveAsLastRecording(audioRecorder as RecorderModel)

                showRecorderError = true
            }
        }
        audioRecorder.onBatchesFolderNotAccessible = {
            scope.launch {
                showBatchesInaccessibleError = true

                runCatching {
                    audioRecorder.stopRecording(context)
                }
                runCatching {
                    audioRecorder.destroyService(context)
                }
            }
        }

        onDispose {
            audioRecorder.onRecordingSave = {}
            audioRecorder.onError = {}
        }
    }

    // Register video recorder events
    DisposableEffect(key1 = videoRecorder, key2 = settings) {
        videoRecorder.onRecordingSave = { justSave ->
            scope.launch {
                if (justSave) {
                    saveRecording(videoRecorder as RecorderModel)
                } else {
                    videoRecorder.stopRecording(context)

                    saveAsLastRecording(videoRecorder as RecorderModel)

                    saveRecording(videoRecorder)

                    videoRecorder.destroyService(context)
                }
            }
        }
        videoRecorder.onRecordingStart = {
            snackbarHostState.currentSnackbarData?.dismiss()
        }
        videoRecorder.onError = {
            scope.launch {
                saveAsLastRecording(videoRecorder as RecorderModel)

                showRecorderError = true
            }
        }
        videoRecorder.onBatchesFolderNotAccessible = {
            scope.launch {
                showBatchesInaccessibleError = true

                runCatching {
                    videoRecorder.stopRecording(context)
                }
                runCatching {
                    videoRecorder.destroyService(context)
                }
            }
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

    if (showBatchesInaccessibleError)
        BatchesInaccessibleDialog(
            onClose = {
                showBatchesInaccessibleError = false
            },
        )
}