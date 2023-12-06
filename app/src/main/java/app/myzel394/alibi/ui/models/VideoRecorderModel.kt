package app.myzel394.alibi.ui.models

import android.Manifest
import android.content.Context
import android.content.Intent
import androidx.camera.core.CameraSelector
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.db.RecordingInformation
import app.myzel394.alibi.enums.RecorderState
import app.myzel394.alibi.helpers.VideoBatchesFolder
import app.myzel394.alibi.services.VideoRecorderService
import app.myzel394.alibi.ui.utils.CameraInfo
import app.myzel394.alibi.ui.utils.PermissionHelper

class VideoRecorderModel :
    BaseRecorderModel<VideoRecorderService.Settings, RecordingInformation, VideoRecorderService, VideoBatchesFolder?>() {
    override var batchesFolder: VideoBatchesFolder? = null
    override val intentClass = VideoRecorderService::class.java

    var enableAudio by mutableStateOf(true)
    var cameraID by mutableIntStateOf(CameraInfo.Lens.BACK.androidValue)

    override val isInRecording: Boolean
        get() = super.isInRecording && recorderService!!.cameraControl != null

    val cameraSelector: CameraSelector
        get() = CameraSelector.Builder().requireLensFacing(cameraID).build()

    fun init(context: Context) {
        enableAudio = PermissionHelper.hasGranted(context, Manifest.permission.RECORD_AUDIO)
        cameraID = CameraInfo.Lens.BACK.androidValue
    }

    override fun onServiceConnected(service: VideoRecorderService) {
        service.settings = VideoRecorderService.Settings.from(settings)

        service.clearAllRecordings()
        service.startRecording()

        recorderState = service.state
        recordingTime = service.recordingTime
    }

    override fun handleIntent(intent: Intent) =
        intent.apply {
            putExtra("cameraID", cameraID)
            putExtra("enableAudio", enableAudio)
        }


    override fun startRecording(context: Context, settings: AppSettings) {
        super.startRecording(context, settings)
    }
}