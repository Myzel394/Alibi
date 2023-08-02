package app.myzel394.locationtest.ui.utils

import kotlin.math.floor


fun formatDuration(durationInMilliseconds: Long): String {
    if (durationInMilliseconds < 1000) {
        return "00:00.$durationInMilliseconds"
    }

    val totalSeconds = durationInMilliseconds / 1000

    if (totalSeconds < 60) {
        return "00:${totalSeconds.toString().padStart(2, '0')}"
    }

    val minutes = floor(totalSeconds / 60.0).toInt()
    val seconds = totalSeconds - (minutes * 60)

    return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}
