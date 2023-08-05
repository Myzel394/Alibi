package app.myzel394.locationtest.ui.utils

import androidx.compose.animation.core.Easing


fun clamp(value: Float, min: Float, max: Float): Float {
    return value
        .coerceAtLeast(min)
        .coerceAtMost(max)
}
