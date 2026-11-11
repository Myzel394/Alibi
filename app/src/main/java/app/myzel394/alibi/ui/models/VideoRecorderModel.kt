package app.myzel394.alibi.ui.models

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.camera.core.CameraSelector
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.documentfile.provider.DocumentFile
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.db.RecordingInformation
import app.myzel394.alibi.enums.RecorderState
import app.myzel394.alibi.helpers.Doctor
import app.myzel394.alibi.helpers.VideoBatchesFolder
import app.myzel394.alibi.services.VideoRecorderService
import app.myzel394.alibi.ui.RECORDER_MEDIA_SELECTED_VALUE
import app.myzel394.alibi.ui.utils.CameraInfo
import app.myzel394.alibi.ui.utils.PermissionHelper

class VideoRecorderModel :
    BaseRecorderModel<RecordingInformation, VideoBatchesFolder, VideoRecorderService>() {
    override var batchesFolder: VideoBatchesFolder? = null
    override val intentClass = VideoRecorderService::class.java

    var enableAudio by mutableStateOf(true)
    var lensFacing by mutableIntStateOf(CameraSelector.LENS_FACING_BACK)
        private set
    var cameraSelection by mutableStateOf<app.myzel394.alibi.db.CameraSelection?>(null)
        private set
    private var explicitCameraSelection = false

    override val isInRecording: Boolean
        get() = super.isInRecording

    var isStartingRecording by mutableStateOf(true)
        private set

    val cameraSelector: CameraSelector
        get() = CameraInfo.selector(lensFacing, cameraSelection)

    fun init(context: Context, settings: AppSettings) {
        enableAudio = PermissionHelper.hasGranted(context, Manifest.permission.RECORD_AUDIO)
        lensFacing = CameraSelector.LENS_FACING_BACK
        cameraSelection = settings.videoRecorderSettings.backCamera
        explicitCameraSelection = false
    }

    fun selectCamera(camera: CameraInfo) {
        lensFacing = camera.lens.androidValue
        cameraSelection = camera.selection
        explicitCameraSelection = true
    }

    override fun startRecording(context: Context, settings: AppSettings) {
        if (!explicitCameraSelection) {
            lensFacing = CameraSelector.LENS_FACING_BACK
            cameraSelection = settings.videoRecorderSettings.backCamera
        }
        batchesFolder = when (settings.saveFolder) {
            null -> VideoBatchesFolder.viaInternalFolder(context)
            RECORDER_MEDIA_SELECTED_VALUE -> VideoBatchesFolder.viaMediaFolder(context)
            else -> VideoBatchesFolder.viaCustomFolder(
                context,
                DocumentFile.fromTreeUri(
                    context,
                    Uri.parse(settings.saveFolder)
                )!!
            )
        }

        super.startRecording(context, settings)
    }

    override fun onServiceConnected(service: VideoRecorderService) {
        lensFacing = service.lensFacing
        cameraSelection = service.cameraSelection
        // A shortcut can start the service before this UI binds. RECORDING is a request
        // state; CameraX may still be opening, so derive readiness from the actual control.
        isStartingRecording = service.cameraControl == null
        service.onCameraControlAvailable = {
            isStartingRecording = service.cameraControl == null
        }
        if (service.state == RecorderState.IDLE) {
            service.prepareForNewRecording()
            service.startRecording()
            onRecordingStart()
        }

        recorderState = service.state
        recordingTime = service.recordingTime
    }

    override fun handleIntent(intent: Intent) =
        intent.apply {
            putExtra("lensFacing", lensFacing)
            putExtra("logicalCameraId", cameraSelection?.logicalId)
            putExtra("physicalCameraId", cameraSelection?.physicalId)
            putExtra("enableAudio", enableAudio)
        }
}