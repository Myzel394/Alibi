package app.myzel394.alibi.ui.utils

import android.content.Context
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.os.Build
import android.util.Size
import android.view.Display
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView

@Composable
fun KeepScreenOn() {
    val currentView = LocalView.current
    DisposableEffect(Unit) {
        currentView.keepScreenOn = true
        onDispose {
            currentView.keepScreenOn = false
        }
    }
}

fun getScreenSize(context: Context): Size {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val bounds = windowManager.currentWindowMetrics.bounds

        Size(bounds.width(), bounds.height())
    } else {
        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val display = displayManager.getDisplay(Display.DEFAULT_DISPLAY)

        val size = Point()
        display.getRealSize(size)

        Size(size.x, size.y)
    }
}


@Composable
fun rememberScreenSize(): Size {
    val context = LocalView.current.context

    return getScreenSize(context)
}
