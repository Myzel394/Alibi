package app.myzel394.alibi.ui.utils

import android.os.Build
import android.view.View
import android.view.WindowManager
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberIsInDarkMode(): Boolean {
    return isSystemInDarkTheme()
}

@Composable
fun ChangeNavColors(
    color: Color
) {
    val activity = LocalContext.current as androidx.activity.ComponentActivity
    val window = activity.window

    DisposableEffect(Unit) {
        val originalStatusBarColor = window.statusBarColor
        val originalNavigationBarColor = window.navigationBarColor

        var systemUiVisibility = 0
        //  Use a dark scrim by default since light nav bar is API 27+
        var navigationBarColor = color
        val winParams = activity.window.attributes

        systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            systemUiVisibility = systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            navigationBarColor = Color.Transparent
        }
        systemUiVisibility = systemUiVisibility or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        window.decorView.systemUiVisibility = systemUiVisibility

        winParams.flags = winParams.flags and
                (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION).inv()
        window.statusBarColor = Color.Transparent.toArgb()
        window.navigationBarColor = navigationBarColor.toArgb()

        window.attributes = winParams

        onDispose {
            window.statusBarColor = originalStatusBarColor
            window.navigationBarColor = originalNavigationBarColor
        }
    }
}
