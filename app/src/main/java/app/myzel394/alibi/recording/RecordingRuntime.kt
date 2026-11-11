package app.myzel394.alibi.recording

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import app.myzel394.alibi.enums.RecorderState
import app.myzel394.alibi.services.RecorderService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Process-local state; services own recording, not the screen or accessibility service. */
object RecordingRuntime {
    private val mutable = MutableStateFlow<Map<String, RecorderState>>(emptyMap())
    val states = mutable.asStateFlow()
    private val services = java.util.concurrent.ConcurrentHashMap<String, RecorderService>()
    val saving = MutableStateFlow(0)
    @Synchronized fun beginSave() { saving.value += 1 }
    @Synchronized fun endSave() { saving.value = (saving.value - 1).coerceAtLeast(0) }
    val isBusy: Boolean get() = services.isNotEmpty() || saving.value > 0
    fun service(type: Class<out RecorderService>): RecorderService? = services[type.name]
    @Synchronized fun update(service: RecorderService, state: RecorderState) {
        services[service.javaClass.name] = service
        mutable.value = mutable.value + (service.javaClass.name to state)
    }
    @Synchronized fun remove(service: RecorderService) {
        if (services.remove(service.javaClass.name, service))
            mutable.value = mutable.value - service.javaClass.name
    }
}

object RecordingHaptics {
    fun signal(context: Context, started: Boolean? = null) {
        val vibrator = if (Build.VERSION.SDK_INT >= 31)
            context.getSystemService(VibratorManager::class.java).defaultVibrator
        else @Suppress("DEPRECATION") (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator)
        if (!vibrator.hasVibrator()) return
        val pattern = when (started) {
            true -> longArrayOf(0, 120)
            false -> longArrayOf(0, 80, 80, 80)
            null -> longArrayOf(0, 50, 60, 50, 60, 50)
        }
        if (Build.VERSION.SDK_INT >= 26) vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        else @Suppress("DEPRECATION") vibrator.vibrate(pattern, -1)
    }
}

/** Noninteractive, small overlay below the system status bar. */
class RecordingIndicator(private val context: Context) {
    private val windows = context.getSystemService(WindowManager::class.java)
    private var dot: View? = null
    fun show() {
        if (dot != null || !Settings.canDrawOverlays(context)) return
        val size = (10 * context.resources.displayMetrics.density).toInt()
        val view = View(context).apply {
            contentDescription = context.getString(app.myzel394.alibi.R.string.recording_indicator)
            background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(Color.RED) }
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        val params = WindowManager.LayoutParams(size, size,
            if (Build.VERSION.SDK_INT >= 26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT).apply {
            gravity = Gravity.TOP or Gravity.END
            x = size * 2
            y = size
        }
        runCatching { windows.addView(view, params); dot = view }
    }
    fun hide() { dot?.let { runCatching { windows.removeView(it) } }; dot = null }
}
