package app.myzel394.alibi.ui.models

import android.Manifest
import android.Manifest.permission.RECORD_AUDIO
import android.content.Context
import android.graphics.Camera
import android.hardware.camera2.CameraManager
import androidx.camera.core.CameraSelector
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import app.myzel394.alibi.ui.utils.PermissionHelper

class VideoRecorderSettingsModel : ViewModel() {
    var enableAudio by mutableStateOf(true)
    var cameraID by mutableIntStateOf(CameraSelector.LENS_FACING_BACK)

    fun init(context: Context) {
        enableAudio = PermissionHelper.hasGranted(context, Manifest.permission.RECORD_AUDIO)
        cameraID = CameraSelector.LENS_FACING_BACK
    }
}