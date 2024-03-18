package app.myzel394.alibi.db

import android.Manifest
import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import app.myzel394.alibi.R
import app.myzel394.alibi.helpers.AudioBatchesFolder
import app.myzel394.alibi.helpers.VideoBatchesFolder
import app.myzel394.alibi.ui.RECORDER_MEDIA_SELECTED_VALUE
import app.myzel394.alibi.ui.SUPPORTS_SCOPED_STORAGE
import app.myzel394.alibi.ui.components.RecorderScreen.organisms.RecorderModel
import app.myzel394.alibi.ui.utils.PermissionHelper
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.LocalDateTime

@Serializable
data class AppSettings(
    val audioRecorderSettings: AudioRecorderSettings = AudioRecorderSettings.getDefaultInstance(),
    val videoRecorderSettings: VideoRecorderSettings = VideoRecorderSettings.getDefaultInstance(),

    val appLockSettings: AppLockSettings? = null,

    val hasSeenOnboarding: Boolean = false,
    val showAdvancedSettings: Boolean = false,
    val theme: Theme = Theme.SYSTEM,
    val lastRecording: RecordingInformation? = null,

    /// Recording information
    // 30 minutes
    val maxDuration: Long = 30 * 60 * 1000L,
    // 60 seconds
    val intervalDuration: Long = 60 * 1000L,

    val notificationSettings: NotificationSettings? = null,
    val deleteRecordingsImmediately: Boolean = false,
    val saveFolder: String? = null,
) {
    fun setShowAdvancedSettings(showAdvancedSettings: Boolean): AppSettings {
        return copy(showAdvancedSettings = showAdvancedSettings)
    }

    fun setAudioRecorderSettings(audioRecorderSettings: AudioRecorderSettings): AppSettings {
        return copy(audioRecorderSettings = audioRecorderSettings)
    }

    fun setVideoRecorderSettings(videoRecorderSettings: VideoRecorderSettings): AppSettings {
        return copy(videoRecorderSettings = videoRecorderSettings)
    }

    fun setNotificationSettings(notificationSettings: NotificationSettings?): AppSettings {
        return copy(notificationSettings = notificationSettings)
    }

    fun setHasSeenOnboarding(hasSeenOnboarding: Boolean): AppSettings {
        return copy(hasSeenOnboarding = hasSeenOnboarding)
    }

    fun setTheme(theme: Theme): AppSettings {
        return copy(theme = theme)
    }

    fun setLastRecording(lastRecording: RecordingInformation?): AppSettings {
        return copy(lastRecording = lastRecording)
    }

    fun setMaxDuration(duration: Long): AppSettings {
        if (duration < 60 * 1000L || duration > 10 * 24 * 60 * 60 * 1000L) {
            throw Exception("Max duration must be between 1 minute and 10 days")
        }

        if (duration < intervalDuration) {
            throw Exception("Max duration must be greater than interval duration")
        }

        return copy(maxDuration = duration)
    }

    fun setIntervalDuration(duration: Long): AppSettings {
        if (duration < 10 * 1000L || duration > 60 * 60 * 1000L) {
            throw Exception("Interval duration must be between 10 seconds and 1 hour")
        }

        if (duration > maxDuration) {
            throw Exception("Interval duration must be less than max duration")
        }

        return copy(intervalDuration = duration)
    }

    fun setDeleteRecordingsImmediately(deleteRecordingsImmediately: Boolean): AppSettings {
        return copy(deleteRecordingsImmediately = deleteRecordingsImmediately)
    }

    fun setSaveFolder(saveFolder: String?): AppSettings {
        return copy(saveFolder = saveFolder)
    }

    fun setAppLockSettings(appLockSettings: AppLockSettings?): AppSettings {
        return copy(appLockSettings = appLockSettings)
    }

    fun saveLastRecording(recorder: RecorderModel): AppSettings {
        return if (deleteRecordingsImmediately) {
            this
        } else {
            setLastRecording(
                recorder.recorderService!!.getRecordingInformation()
            )
        }
    }

    // If the object is present, biometric authentication is enabled.
    // To disable biometric authentication, set the instance to null.
    fun isAppLockEnabled() = appLockSettings != null

    fun requiresExternalStoragePermission(context: Context): Boolean {
        return !SUPPORTS_SCOPED_STORAGE && (saveFolder == RECORDER_MEDIA_SELECTED_VALUE && !PermissionHelper.hasGranted(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ))
    }

    enum class Theme {
        SYSTEM,
        LIGHT,
        DARK,
    }

    fun exportToString(): String {
        return Json.encodeToString(serializer(), this)
    }

    companion object {
        fun getDefaultInstance(): AppSettings = AppSettings()

        fun fromExportedString(data: String): AppSettings {
            return Json.decodeFromString(
                serializer(),
                data,
            )
        }
    }
}

@Serializable
data class RecordingInformation(
    val folderPath: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val recordingStart: LocalDateTime,
    val maxDuration: Long,
    val intervalDuration: Long,
    val fileExtension: String,
    val type: Type,
) {
    fun hasRecordingsAvailable(context: Context): Boolean =
        when (type) {
            Type.AUDIO -> AudioBatchesFolder.importFromFolder(folderPath, context)
                .hasRecordingsAvailable()

            Type.VIDEO -> VideoBatchesFolder.importFromFolder(folderPath, context)
                .hasRecordingsAvailable()
        }

    enum class Type {
        AUDIO,
        VIDEO,
    }
}

@Serializable
data class AudioRecorderSettings(
    // 320 Kbps
    val bitRate: Int = 320000,
    val samplingRate: Int? = null,
    val outputFormat: Int? = null,
    val encoder: Int? = null,
    val showAllMicrophones: Boolean = false,
) {
    fun getOutputFormat(): Int {
        if (outputFormat != null) {
            return outputFormat
        }

        if (encoder == null) {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                MediaRecorder.OutputFormat.AAC_ADTS
            else MediaRecorder.OutputFormat.THREE_GPP
        }

        return when (encoder) {
            MediaRecorder.AudioEncoder.AAC -> MediaRecorder.OutputFormat.AAC_ADTS
            MediaRecorder.AudioEncoder.AAC_ELD -> MediaRecorder.OutputFormat.AAC_ADTS
            MediaRecorder.AudioEncoder.AMR_NB -> MediaRecorder.OutputFormat.AMR_NB
            MediaRecorder.AudioEncoder.AMR_WB -> MediaRecorder.OutputFormat.AMR_WB
            MediaRecorder.AudioEncoder.HE_AAC -> MediaRecorder.OutputFormat.AAC_ADTS
            MediaRecorder.AudioEncoder.VORBIS -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaRecorder.OutputFormat.OGG
                } else {
                    MediaRecorder.OutputFormat.AAC_ADTS
                }
            }

            MediaRecorder.AudioEncoder.OPUS -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaRecorder.OutputFormat.OGG
                } else {
                    MediaRecorder.OutputFormat.AAC_ADTS
                }
            }

            else -> MediaRecorder.OutputFormat.DEFAULT
        }
    }

    fun getMimeType(): String = when (getOutputFormat()) {
        MediaRecorder.OutputFormat.AAC_ADTS -> "audio/aac"
        MediaRecorder.OutputFormat.THREE_GPP -> "audio/3gpp"
        MediaRecorder.OutputFormat.MPEG_4 -> "audio/mp4"
        MediaRecorder.OutputFormat.MPEG_2_TS -> "audio/ts"
        MediaRecorder.OutputFormat.WEBM -> "audio/webm"
        MediaRecorder.OutputFormat.AMR_NB -> "audio/amr"
        MediaRecorder.OutputFormat.AMR_WB -> "audio/amr-wb"
        MediaRecorder.OutputFormat.OGG -> "audio/ogg"
        else -> "audio/3gpp"
    }

    fun getSamplingRate(): Int = samplingRate ?: when (getOutputFormat()) {
        MediaRecorder.OutputFormat.AAC_ADTS -> 96000
        MediaRecorder.OutputFormat.THREE_GPP -> 44100
        MediaRecorder.OutputFormat.MPEG_4 -> 44100
        MediaRecorder.OutputFormat.MPEG_2_TS -> 48000
        MediaRecorder.OutputFormat.WEBM -> 48000
        MediaRecorder.OutputFormat.AMR_NB -> 8000
        MediaRecorder.OutputFormat.AMR_WB -> 16000
        MediaRecorder.OutputFormat.OGG -> 48000
        else -> 48000
    }

    fun getEncoder(): Int = encoder ?: if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        MediaRecorder.AudioEncoder.AAC
    else
        MediaRecorder.AudioEncoder.AMR_NB

    fun setBitRate(bitRate: Int): AudioRecorderSettings {
        if (bitRate !in 1000..320000) {
            throw Exception("Bit rate must be between 1000 and 320000")
        }

        return copy(bitRate = bitRate)
    }

    fun setSamplingRate(samplingRate: Int?): AudioRecorderSettings {
        if (samplingRate != null && samplingRate < 1000) {
            throw Exception("Sampling rate must be at least 1000")
        }

        return copy(samplingRate = samplingRate)
    }

    fun setOutputFormat(outputFormat: Int?): AudioRecorderSettings {
        if (outputFormat != null && (outputFormat < 0 || outputFormat > 11)) {
            throw Exception("OutputFormat is not a MediaRecorder.OutputFormat constant")
        }

        return copy(outputFormat = outputFormat)
    }

    fun setEncoder(encoder: Int?): AudioRecorderSettings {
        if (encoder != null && (encoder < 0 || encoder > 7)) {
            throw Exception("Encoder is not a MediaRecorder.AudioEncoder constant")
        }

        return copy(encoder = encoder)
    }

    fun setShowAllMicrophones(showAllMicrophones: Boolean): AudioRecorderSettings {
        return copy(showAllMicrophones = showAllMicrophones)
    }

    fun isEncoderCompatible(encoder: Int): Boolean {
        if (outputFormat == null || outputFormat == MediaRecorder.OutputFormat.DEFAULT) {
            return true
        }

        val supportedFormats = ENCODER_SUPPORTED_OUTPUT_FORMATS_MAP[encoder]!!

        return supportedFormats.contains(outputFormat)
    }

    val fileExtension: String
        get() = when (getOutputFormat()) {
            MediaRecorder.OutputFormat.AAC_ADTS -> "aac"
            MediaRecorder.OutputFormat.THREE_GPP -> "3gp"
            MediaRecorder.OutputFormat.MPEG_4 -> "mp4"
            MediaRecorder.OutputFormat.MPEG_2_TS -> "ts"
            MediaRecorder.OutputFormat.WEBM -> "webm"
            MediaRecorder.OutputFormat.AMR_NB -> "amr"
            MediaRecorder.OutputFormat.AMR_WB -> "awb"
            MediaRecorder.OutputFormat.OGG -> "ogg"
            else -> "raw"
        }

    companion object {
        fun getDefaultInstance(): AudioRecorderSettings = AudioRecorderSettings()
        val EXAMPLE_MAX_DURATIONS = listOf(
            15 * 60 * 1000L,
            30 * 60 * 1000L,
            60 * 60 * 1000L,
            2 * 60 * 60 * 1000L,
            3 * 60 * 60 * 1000L,
        )
        val EXAMPLE_DURATION_TIMES = listOf(
            60 * 1000L,
            60 * 5 * 1000L,
            60 * 10 * 1000L,
            60 * 15 * 1000L,
        )
        val EXAMPLE_BITRATE_VALUES = listOf(
            96 * 1000,
            128 * 1000,
            160 * 1000,
            192 * 1000,
            256 * 1000,
            320 * 1000,
        )
        val EXAMPLE_SAMPLING_RATE = listOf(
            null,
            8000,
            16000,
            22050,
            44100,
            48000,
            96000,
        )
        val OUTPUT_FORMAT_INDEX_TEXT_MAP = mapOf(
            0 to "Default",
            1 to "THREE_GPP",
            2 to "MPEG_4",
            3 to "AMR_NB",
            4 to "AMR_WB",
            5 to "AAC_ADIF",
            6 to "AAC_ADTS",
            7 to "OUTPUT_FORMAT_RTP_AVP",
            8 to "MPEG_2_TS",
            9 to "WEBM",
            10 to "HEIF",
            11 to "OGG",
        )
        val ENCODER_INDEX_TEXT_MAP = mapOf(
            0 to "Default",
            1 to "AMR_NB",
            2 to "AMR_WB",
            3 to "AAC",
            4 to "HE_AAC",
            5 to "AAC_ELD",
            6 to "VORBIS",
            7 to "OPUS",
        )
        val ENCODER_SUPPORTED_OUTPUT_FORMATS_MAP: Map<Int, Array<Int>> = mutableMapOf(
            MediaRecorder.AudioEncoder.DEFAULT to arrayOf(
                MediaRecorder.OutputFormat.DEFAULT,
            ),
            MediaRecorder.AudioEncoder.AAC to arrayOf(
                MediaRecorder.OutputFormat.THREE_GPP,
                MediaRecorder.OutputFormat.MPEG_4,
                MediaRecorder.OutputFormat.AAC_ADTS,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) MediaRecorder.OutputFormat.MPEG_2_TS else null,
            ).filterNotNull().toTypedArray(),
            MediaRecorder.AudioEncoder.AAC_ELD to arrayOf(
                MediaRecorder.OutputFormat.THREE_GPP,
                MediaRecorder.OutputFormat.MPEG_4,
                MediaRecorder.OutputFormat.AAC_ADTS,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) MediaRecorder.OutputFormat.MPEG_2_TS else null,
            ).filterNotNull().toTypedArray(),
            MediaRecorder.AudioEncoder.AMR_NB to arrayOf(
                MediaRecorder.OutputFormat.THREE_GPP,
                MediaRecorder.OutputFormat.AMR_NB,
            ),
            MediaRecorder.AudioEncoder.AMR_WB to arrayOf(
                MediaRecorder.OutputFormat.THREE_GPP,
                MediaRecorder.OutputFormat.AMR_WB,
            ),
            MediaRecorder.AudioEncoder.HE_AAC to arrayOf(
                MediaRecorder.OutputFormat.THREE_GPP,
                MediaRecorder.OutputFormat.MPEG_4,
                MediaRecorder.OutputFormat.AAC_ADTS,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) MediaRecorder.OutputFormat.MPEG_2_TS else null,
            ).filterNotNull().toTypedArray(),
            MediaRecorder.AudioEncoder.VORBIS to arrayOf(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaRecorder.OutputFormat.OGG else null,
                MediaRecorder.OutputFormat.MPEG_4
            ).filterNotNull().toTypedArray(),
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaRecorder.AudioEncoder.OPUS, arrayOf(MediaRecorder.OutputFormat.OGG))
            }
        }.toMap()
        val OUTPUT_FORMATS_SUPPORTED_ENCODER_MAP = (mutableMapOf<Int, Array<Int>>().also { map ->
            ENCODER_SUPPORTED_OUTPUT_FORMATS_MAP.forEach { (encoder, formats) ->
                formats.forEach { format ->
                    if (map.containsKey(format)) {
                        map[format]!!.plus(encoder)
                    } else {
                        map[format] = arrayOf(encoder)
                    }
                }
            }
        }).toMap()
    }
}

@Serializable
data class VideoRecorderSettings(
    val targetedVideoBitRate: Int? = null,
    val quality: String? = null,
    val targetFrameRate: Int? = null,
) {
    fun setTargetedVideoBitRate(bitRate: Int?): VideoRecorderSettings {
        return copy(targetedVideoBitRate = bitRate)
    }

    fun setQuality(quality: Quality?): VideoRecorderSettings {
        val invertedMap = QUALITY_NAME_QUALITY_MAP.entries.associateBy({ it.value }, { it.key })

        return copy(quality = quality?.let { invertedMap[it] })
    }

    fun setTargetFrameRate(frameRate: Int?): VideoRecorderSettings {
        return copy(targetFrameRate = frameRate)
    }

    fun getQuality(): Quality? =
        quality?.let {
            QUALITY_NAME_QUALITY_MAP[it]!!
        }

    fun getQualitySelector(): QualitySelector? =
        quality?.let {
            QualitySelector.from(
                QUALITY_NAME_QUALITY_MAP[it]!!
            )
        }

    fun getMimeType() = "video/$fileExtension"

    val fileExtension
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) "mp4" else "3gp"

    companion object {
        fun getDefaultInstance() = VideoRecorderSettings()

        val QUALITY_NAME_QUALITY_MAP: Map<String, Quality> = mapOf(
            "LOWEST" to Quality.LOWEST,
            "HIGHEST" to Quality.HIGHEST,
            "SD" to Quality.SD,
            "HD" to Quality.HD,
            "FHD" to Quality.FHD,
            "UHD" to Quality.UHD,
        )

        val EXAMPLE_BITRATE_VALUES = listOf(
            null,
            500 * 1000,
            // 1 Mbps
            1 * 1000 * 1000,
            2 * 1000 * 1000,
            4 * 1000 * 1000,
            8 * 1000 * 1000,
            16 * 1000 * 1000,
            32 * 1000 * 1000,
            50 * 1000 * 1000,
            100 * 1000 * 1000,
        )

        val EXAMPLE_FRAME_RATE_VALUES = listOf(
            null,
            24,
            30,
            60,
            120,
            240,
        )

        val AVAILABLE_QUALITIES = listOf(
            Quality.HIGHEST,
            Quality.UHD,
            Quality.FHD,
            Quality.HD,
            Quality.SD,
            Quality.LOWEST,
        )

        val EXAMPLE_QUALITY_VALUES = listOf(
            null,
        ) + AVAILABLE_QUALITIES
    }
}

@Serializable
data class NotificationSettings(
    val title: String,
    val message: String,
    val iconID: Int,
    val showOngoing: Boolean,
    val preset: Preset? = null,
) {
    @Serializable
    sealed class Preset(
        val titleID: Int,
        val messageID: Int,
        val showOngoing: Boolean,
        val iconID: Int,
    ) {
        @Serializable
        data object Default : Preset(
            R.string.ui_audioRecorder_state_recording_title,
            R.string.ui_recorder_state_recording_description,
            true,
            R.drawable.launcher_monochrome_noopacity,
        )

        @Serializable
        data object Weather : Preset(
            R.string.ui_recorder_state_recording_fake_weather_title,
            R.string.ui_recorder_state_recording_fake_weather_description,
            false,
            R.drawable.ic_cloud
        )

        @Serializable
        data object Player : Preset(
            R.string.ui_recorder_state_recording_fake_player_title,
            R.string.ui_recorder_state_recording_fake_player_description,
            true,
            R.drawable.ic_note,
        )

        @Serializable
        data object Browser : Preset(
            R.string.ui_recorder_state_recording_fake_browser_title,
            R.string.ui_recorder_state_recording_fake_browser_description,
            true,
            R.drawable.ic_download,
        )

        @Serializable
        data object VPN : Preset(
            R.string.ui_recorder_state_recording_fake_vpn_title,
            R.string.ui_recorder_state_recording_fake_vpn_description,
            false,
            R.drawable.ic_vpn,
        )
    }

    companion object {
        fun fromPreset(preset: Preset): NotificationSettings {
            return NotificationSettings(
                title = "",
                message = "",
                showOngoing = preset.showOngoing,
                iconID = preset.iconID,
                preset = preset,
            )
        }

        val PRESETS = listOf(
            Preset.Default,
            Preset.Weather,
            Preset.Player,
            Preset.Browser,
            Preset.VPN,
        )
    }
}

@Serializable
class AppLockSettings {
    companion object {
        fun getDefaultInstance() = AppLockSettings()
    }
}
