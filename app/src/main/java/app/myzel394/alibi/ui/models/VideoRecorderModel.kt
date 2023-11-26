package app.myzel394.alibi.ui.models

import app.myzel394.alibi.db.RecordingInformation
import app.myzel394.alibi.services.VideoRecorderService

class VideoRecorderModel :
    BaseRecorderModel<VideoRecorderService.Settings, RecordingInformation, VideoRecorderService>() {
    override val intentClass = VideoRecorderService::class.java

    override fun onServiceConnected(service: VideoRecorderService) {
        service.settings = VideoRecorderService.Settings.from()

        service.clearAllRecordings()
        service.startRecording()

        recorderState = service.state
        recordingTime = service.recordingTime
    }
}