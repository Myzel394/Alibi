package app.myzel394.locationtest.ui.utils

import kotlin.math.floor


fun formatDuration(
    durationInMilliseconds: Long,
    formatFull: Boolean = false,
): String {
    val totalSeconds = durationInMilliseconds / 1000

    val hours = floor(totalSeconds / 3600.0).toInt()
    val minutes = floor(totalSeconds / 60.0).toInt() % 60
    val seconds = totalSeconds - (minutes * 60)

    if (formatFull) {
        return "" +
                hours.toString().padStart(2, '0') +
            ":" + minutes.toString().padStart(2, '0') +
            ":" + seconds.toString().padStart(2, '0') +
            "." + (durationInMilliseconds % 1000).toString()
    }

    if (durationInMilliseconds < 1000) {
        return "00:00.$durationInMilliseconds"
    }

    if (totalSeconds < 60) {
        return "00:${totalSeconds.toString().padStart(2, '0')}"
    }

    return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}
