package app.myzel394.alibi.recording

import app.myzel394.alibi.enums.RecorderState
import app.myzel394.alibi.services.RecorderService
import app.myzel394.alibi.services.VideoRecorderService
import app.myzel394.alibi.ui.models.VideoRecorderModel
import org.junit.Assert.*
import org.junit.Test

class VideoStartupBindingTest {
    @Test fun bindingToStartingBackgroundVideoKeepsControlsPendingUntilCameraExists() {
        // Reproduce the real callback ordering: recording was requested before camera binding finished.
        val service = VideoRecorderService()
        RecorderService::class.java.getDeclaredField("state").apply { isAccessible = true }
            .set(service, RecorderState.RECORDING)
        assertNull(service.cameraControl)
        val model = VideoRecorderModel()
        VideoRecorderModel::class.java.getDeclaredMethod("onServiceConnected", VideoRecorderService::class.java)
            .apply { isAccessible = true }.invoke(model, service)
        assertTrue("Recording state alone does not imply that CameraX controls are ready", model.isStartingRecording)
    }
}
