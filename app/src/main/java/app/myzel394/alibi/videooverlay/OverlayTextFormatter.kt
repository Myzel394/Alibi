package app.myzel394.alibi.videooverlay

import app.myzel394.alibi.db.VideoOverlaySettings
import app.myzel394.alibi.db.VideoOverlayTimeFormat
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

data class OverlayLocation(
    val latitude: Double,
    val longitude: Double,
)

class OverlayTextFormatter(
    private val clock: Clock = Clock.systemDefaultZone(),
    private val locale: Locale = Locale.US,
) {
    fun buildOverlayText(
        settings: VideoOverlaySettings,
        location: OverlayLocation?,
    ): String {
        return buildList {
            if (settings.timeEnabled) {
                add(formatTime(settings.timeFormat))
            }
            if (settings.locationEnabled) {
                add(formatLocation(location))
            }
        }.joinToString("\n")
    }

    fun formatTime(format: VideoOverlayTimeFormat): String {
        val instant = clock.instant().truncatedTo(ChronoUnit.SECONDS)
        val zoneId = clock.zone

        return formatTime(format, instant, zoneId)
    }

    fun formatTime(
        format: VideoOverlayTimeFormat,
        instant: Instant,
        zoneId: ZoneId,
    ): String {
        val truncatedInstant = instant.truncatedTo(ChronoUnit.SECONDS)

        return when (format) {
            VideoOverlayTimeFormat.ISO_OFFSET_DATE_TIME ->
                DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(
                    truncatedInstant.atZone(zoneId).toOffsetDateTime()
                )

            VideoOverlayTimeFormat.ISO_INSTANT ->
                DateTimeFormatter.ISO_INSTANT.format(truncatedInstant)

            VideoOverlayTimeFormat.ISO_LOCAL_DATE_TIME ->
                DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(
                    LocalDateTime.ofInstant(truncatedInstant, zoneId)
                )

            VideoOverlayTimeFormat.DASHCAM_DATE_TIME ->
                DASHCAM_DATE_TIME_FORMATTER.format(
                    LocalDateTime.ofInstant(truncatedInstant, zoneId)
                )
        }
    }

    fun formatLocation(location: OverlayLocation?): String {
        if (location == null) {
            return LOCATION_WAITING_TEXT
        }

        return String.format(
            locale,
            "GPS %.6f, %.6f",
            location.latitude,
            location.longitude,
        )
    }

    companion object {
        const val LOCATION_WAITING_TEXT = "GPS waiting"
        private val DASHCAM_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
            "yyyy-MM-dd HH:mm:ss",
            Locale.US,
        )
    }
}
