package app.myzel394.alibi.ui.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import app.myzel394.alibi.db.RecordingInformation
import app.myzel394.alibi.enums.RecorderState
import app.myzel394.alibi.services.AudioRecorderService
import app.myzel394.alibi.services.VideoService

class VideoRecorderModel :
    BaseRecorderModel<VideoService.Settings, RecordingInformation, VideoService>() {
    override val intentClass = VideoService::class.java

    override fun onServiceConnected(service: VideoService) {
        service.settings = VideoService.Settings.from()

        service.clearAllRecordings()
        service.startRecording()

        recorderState = service.state
        recordingTime = service.recordingTime
    }
}