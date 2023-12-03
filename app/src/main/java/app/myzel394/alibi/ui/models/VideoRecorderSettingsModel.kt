package app.myzel394.alibi.ui.models

import android.graphics.Camera
import android.hardware.camera2.CameraManager
import androidx.camera.core.CameraSelector
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class VideoRecorderSettingsModel : ViewModel() {
    var enableAudio by mutableStateOf(true)
    var cameraID by mutableIntStateOf(0)
}