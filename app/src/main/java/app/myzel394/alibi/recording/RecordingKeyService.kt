package app.myzel394.alibi.recording

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import app.myzel394.alibi.R
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.services.RecorderService
import app.myzel394.alibi.services.VideoRecorderService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Only filters keys. Does not request screen contents or observe other applications. */
class RecordingKeyService : AccessibilityService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val handler = Handler(Looper.getMainLooper())
    private var enabled = false
    private val presses = mapOf(KeyEvent.KEYCODE_VOLUME_DOWN to VolumeKeyPress(), KeyEvent.KEYCODE_VOLUME_UP to VolumeKeyPress())
    private val triggers = presses.mapValues { (key, press) -> Runnable {
        if (enabled && press.timeout()) toggle(if (key == KeyEvent.KEYCODE_VOLUME_UP) ShortcutCamera.FRONT else ShortcutCamera.BACK)
    } }
    override fun onServiceConnected() {
        serviceInfo = serviceInfo.apply {
            flags = flags or android.accessibilityservice.AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
        }
        scope.launch { dataStore.data.collect {
            enabled = it.hardwareShortcutEnabled
            if (!enabled) triggers.values.forEach(handler::removeCallbacks)
        } }
    }
    override fun onKeyEvent(event: KeyEvent): Boolean {
        val press = presses[event.keyCode] ?: return false
        val trigger = triggers.getValue(event.keyCode)
        if (!enabled && !press.pressed) return false
        when (event.action) {
            KeyEvent.ACTION_DOWN -> if (press.down(event.repeatCount)) {
                handler.postDelayed(trigger, LONG_PRESS_MS)
            }
            KeyEvent.ACTION_UP -> {
                handler.removeCallbacks(trigger)
                if (press.up(event.isCanceled)) getSystemService(AudioManager::class.java).adjustVolume(
                    if (event.keyCode == KeyEvent.KEYCODE_VOLUME_UP) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER,
                    AudioManager.FLAG_SHOW_UI)
            }
        }
        return true
    }
    private fun toggle(requested: ShortcutCamera) {
        val video = RecordingRuntime.service(VideoRecorderService::class.java) as? VideoRecorderService
        val active = video?.let { if (it.lensFacing == androidx.camera.core.CameraSelector.LENS_FACING_FRONT) ShortcutCamera.FRONT else ShortcutCamera.BACK }
        when (cameraShortcutDecision(requested, active, RecordingRuntime.isBusy)) {
            ShortcutDecision.STOP -> { video!!.requestStopFromShortcut(); return }
            ShortcutDecision.BUSY -> {
                RecordingHaptics.signal(this)
                Toast.makeText(this, R.string.shortcut_camera_busy, Toast.LENGTH_LONG).show()
                return
            }
            ShortcutDecision.START -> Unit
        }
        val request = ShortcutRequest.issue()
        scope.launch {
            try {
                val settings = dataStore.data.first()
                if (!settings.hardwareShortcutEnabled || !ShortcutRequest.matches(request)) {
                    ShortcutRequest.cancel(request)
                    return@launch
                }
                val missingCamera = androidx.core.content.ContextCompat.checkSelfPermission(this@RecordingKeyService,
                    android.Manifest.permission.CAMERA) != android.content.pm.PackageManager.PERMISSION_GRANTED
                val missingNotifications = android.os.Build.VERSION.SDK_INT >= 33 && androidx.core.content.ContextCompat.checkSelfPermission(
                    this@RecordingKeyService, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED
                if (missingCamera || missingNotifications) {
                    ShortcutRequest.cancel(request)
                    RecordingHaptics.signal(this@RecordingKeyService)
                    Toast.makeText(this@RecordingKeyService, if (missingCamera) R.string.shortcut_permissions_required
                        else R.string.shortcut_notifications_required, Toast.LENGTH_LONG).show()
                    return@launch
                }
                val command = Intent(this@RecordingKeyService, VideoRecorderService::class.java).apply {
                    action = RecorderService.ACTION_START_VIDEO
                    putExtra("settings", settings.exportToString())
                    val lensFacing = if (requested == ShortcutCamera.FRONT) androidx.camera.core.CameraSelector.LENS_FACING_FRONT else androidx.camera.core.CameraSelector.LENS_FACING_BACK
                    putExtra("lensFacing", lensFacing)
                    if (requested == ShortcutCamera.BACK) settings.videoRecorderSettings.backCamera?.let {
                        putExtra("logicalCameraId", it.logicalId)
                        putExtra("physicalCameraId", it.physicalId)
                    }
                    putExtra("shortcutToken", request)
                    putExtra("enableAudio", androidx.core.content.ContextCompat.checkSelfPermission(
                        this@RecordingKeyService, android.Manifest.permission.RECORD_AUDIO) == android.content.pm.PackageManager.PERMISSION_GRANTED)
                }
                androidx.core.content.ContextCompat.startForegroundService(this@RecordingKeyService, command)
                handler.postDelayed({
                    if (ShortcutRequest.cancel(request)) {
                        RecordingHaptics.signal(this@RecordingKeyService)
                        Toast.makeText(this@RecordingKeyService, R.string.shortcut_background_blocked, Toast.LENGTH_LONG).show()
                    }
                }, ShortcutRequest.TIMEOUT_MS)
            } catch (error: Exception) {
                ShortcutRequest.cancel(request)
                android.util.Log.e("AlibiShortcut", "Background recording start rejected", error)
                RecordingHaptics.signal(this@RecordingKeyService)
                Toast.makeText(this@RecordingKeyService, R.string.shortcut_start_failed, Toast.LENGTH_LONG).show()
            }
        }
    }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit
    override fun onInterrupt() {
        triggers.values.forEach(handler::removeCallbacks)
        presses.values.forEach(VolumeKeyPress::reset)
    }
    override fun onDestroy() {
        onInterrupt()
        handler.removeCallbacksAndMessages(null)
        ShortcutRequest.clear()
        scope.cancel()
        super.onDestroy()
    }
    companion object { const val LONG_PRESS_MS = 800L }
}
