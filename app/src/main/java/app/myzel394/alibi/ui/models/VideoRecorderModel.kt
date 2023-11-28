package app.myzel394.alibi.ui.models

import app.myzel394.alibi.db.RecordingInformation
import app.myzel394.alibi.helpers.VideoBatchesFolder
import app.myzel394.alibi.services.VideoRecorderService

class VideoRecorderModel :
    BaseRecorderModel<VideoRecorderService.Settings, RecordingInformation, VideoRecorderService, VideoBatchesFolder?>() {
    override var batchesFolder: VideoBatchesFolder? = null
    override val intentClass = VideoRecorderService::class.java

    override fun onServiceConnected(service: VideoRecorderService) {
        service.settings = VideoRecorderService.Settings.from(settings)

        service.clearAllRecordings()
        service.startRecording()

        recorderState = service.state
        recordingTime = service.recordingTime
    }
}