package app.myzel394.alibi.ui.utils

import java.time.LocalDateTime


fun isSameDay(date1: LocalDateTime, date2: LocalDateTime): Boolean {
    return date1.year == date2.year && date1.dayOfYear == date2.dayOfYear
}