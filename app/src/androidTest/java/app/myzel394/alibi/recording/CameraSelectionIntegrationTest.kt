package app.myzel394.alibi.recording

import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.CameraSelector
import androidx.core.content.ContextCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import app.myzel394.alibi.MainActivity
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.db.VideoRecorderSettings
import app.myzel394.alibi.services.RecorderService
import app.myzel394.alibi.services.VideoRecorderService
import app.myzel394.alibi.ui.utils.CameraInfo
import app.myzel394.alibi.ui.utils.getCameraProvider
import kotlinx.coroutines.*
import org.junit.Assert.*
import org.junit.Test

@androidx.annotation.OptIn(ExperimentalCamera2Interop::class)
class CameraSelectionIntegrationTest {
    @Test fun catalogSelectionsResolveToTheirActualCameraIdsAndFacing() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val provider = context.getCameraProvider()
        val manager = context.getSystemService(CameraManager::class.java)
        val catalog = CameraInfo.querySelectableCameras(context)
        assertTrue(catalog.any { it.lens == CameraInfo.Lens.BACK })
        assertTrue(catalog.any { it.lens == CameraInfo.Lens.FRONT })
        for (camera in catalog) {
            val info = provider.getCameraInfo(CameraInfo.selector(camera.lens.androidValue, camera.selection))
            assertEquals(camera.id, Camera2CameraInfo.from(info).cameraId)
            assertEquals(manager.getCameraCharacteristics(camera.id).get(CameraCharacteristics.LENS_FACING), camera.lens.androidValue)
        }
    }

    @Test fun frontAndRearRequestsOpenTheRequestedCameraAndStopCleanly() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        ActivityScenario.launch(MainActivity::class.java).use {
            for (facing in listOf(CameraSelector.LENS_FACING_FRONT, CameraSelector.LENS_FACING_BACK)) {
                val token = ShortcutRequest.issue()
                val settings = AppSettings(hardwareShortcutEnabled = true, maxDuration = 60000, intervalDuration = 10000,
                    videoRecorderSettings = VideoRecorderSettings(quality = "SD"))
                val command = Intent(context, VideoRecorderService::class.java).apply {
                    action = RecorderService.ACTION_START_VIDEO
                    putExtra("shortcutToken", token)
                    putExtra("settings", settings.exportToString())
                    putExtra("lensFacing", facing)
                    putExtra("enableAudio", false)
                }
                var active: VideoRecorderService? = null
                try {
                    withContext(Dispatchers.Main) { ContextCompat.startForegroundService(context, command) }
                    withTimeout(15000) {
                        while (active == null) {
                            active = withContext(Dispatchers.Main) {
                                (RecordingRuntime.service(VideoRecorderService::class.java) as? VideoRecorderService)
                                    ?.takeIf { service -> service.cameraControl != null }
                            }
                            delay(50)
                        }
                    }
                    val actual = withContext(Dispatchers.Main) {
                        Camera2CameraInfo.from(active!!.cameraControl!!.camera.cameraInfo)
                            .getCameraCharacteristic(CameraCharacteristics.LENS_FACING)
                    }
                    assertEquals(facing, actual)
                    delay(600)
                } finally {
                    withContext(Dispatchers.Main) {
                        (RecordingRuntime.service(VideoRecorderService::class.java) as? VideoRecorderService)?.requestStopFromShortcut(false)
                    }
                    withTimeout(15000) { while (RecordingRuntime.isBusy) delay(50) }
                }
            }
        }
    }
}
