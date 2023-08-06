package app.myzel394.alibi.ui.utils


fun clamp(value: Float, min: Float, max: Float): Float {
    return value
        .coerceAtLeast(min)
        .coerceAtMost(max)
}
