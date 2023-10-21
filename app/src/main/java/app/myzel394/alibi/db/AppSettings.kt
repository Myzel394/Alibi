package app.myzel394.alibi.db

import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_DATE_TIME

@Serializable
data class AppSettings(
    val audioRecorderSettings: AudioRecorderSettings = AudioRecorderSettings(),
    val hasSeenOnboarding: Boolean = false,
    val showAdvancedSettings: Boolean = false,
) {
    fun setShowAdvancedSettings(showAdvancedSettings: Boolean): AppSettings {
        return copy(showAdvancedSettings = showAdvancedSettings)
    }

    fun setAudioRecorderSettings(audioRecorderSettings: AudioRecorderSettings): AppSettings {
        return copy(audioRecorderSettings = audioRecorderSettings)
    }

    fun setHasSeenOnboarding(hasSeenOnboarding: Boolean): AppSettings {
        return copy(hasSeenOnboarding = hasSeenOnboarding)
    }

    companion object {
        fun getDefaultInstance(): AppSettings = AppSettings()
    }
}

@Serializable
data class LastRecording(
    val folderPath: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val recordingStart: LocalDateTime,
    val maxDuration: Long,
    val intervalDuration: Long,
    val fileExtension: String,
    val forceExactMaxDuration: Boolean,
) {
    val fileFolder: File
        get() = File(folderPath)

    val filePaths: List<File>
        get() =
            File(folderPath).listFiles()?.filter {
                val name = it.nameWithoutExtension

                name.toIntOrNull() != null
            }?.toList() ?: emptyList()

    val hasRecordingAvailable: Boolean
        get() = filePaths.isNotEmpty()

    private fun stripConcatenatedFileToExactDuration(
        outputFile: File
    ) {
        // Move the concatenated file to a temporary file
        val rawFile = File("$folderPath/${outputFile.nameWithoutExtension}-raw.${fileExtension}")
        outputFile.renameTo(rawFile)

        val command = "-sseof ${maxDuration / -1000} -i $rawFile -y $outputFile"

        val session = FFmpegKit.execute(command)

        if (!ReturnCode.isSuccess(session.returnCode)) {
            Log.d(
                "Audio Concatenation",
                String.format(
                    "Command failed with state %s and rc %s.%s",
                    session.getState(),
                    session.getReturnCode(),
                    session.getFailStackTrace()
                )
            )

            throw Exception("Failed to strip concatenated audio")
        }
    }

    suspend fun concatenateFiles(forceConcatenation: Boolean = false): File {
        val paths = filePaths.joinToString("|")
        val fileName = recordingStart
            .format(ISO_DATE_TIME)
            .toString()
            .replace(":", "-")
            .replace(".", "_")
        val outputFile = File("$fileFolder/$fileName.${fileExtension}")

        if (outputFile.exists() && !forceConcatenation) {
            return outputFile
        }

        val command = "-i 'concat:$paths' -y" +
                " -acodec copy" +
                " -metadata title='$fileName' " +
                " -metadata date='${recordingStart.format(ISO_DATE_TIME)}'" +
                " -metadata batch_count='${filePaths.size}'" +
                " -metadata batch_duration='${intervalDuration}'" +
                " -metadata max_duration='${maxDuration}'" +
                " $outputFile"

        val session = FFmpegKit.execute(command)

        if (!ReturnCode.isSuccess(session.returnCode)) {
            Log.d(
                "Audio Concatenation",
                String.format(
                    "Command failed with state %s and rc %s.%s",
                    session.getState(),
                    session.getReturnCode(),
                    session.getFailStackTrace()
                )
            )

            throw Exception("Failed to concatenate audios")
        }

        val minRequiredForPossibleInExactMaxDuration = maxDuration / intervalDuration
        if (forceExactMaxDuration && filePaths.size > minRequiredForPossibleInExactMaxDuration) {
            stripConcatenatedFileToExactDuration(outputFile)
        }

        return outputFile
    }
}

@Serializable
data class AudioRecorderSettings(
    val maxDuration: Long = 30 * 60 * 1000L,
    // 60 seconds
    val intervalDuration: Long = 60 * 1000L,
    val forceExactMaxDuration: Boolean = true,
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

    fun setIntervalDuration(duration: Long): AudioRecorderSettings {
        if (duration < 10 * 1000L || duration > 60 * 60 * 1000L) {
            throw Exception("Interval duration must be between 10 seconds and 1 hour")
        }

        if (duration > maxDuration) {
            throw Exception("Interval duration must be less than max duration")
        }

        return copy(intervalDuration = duration)
    }

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

    fun setMaxDuration(duration: Long): AudioRecorderSettings {
        if (duration < 60 * 1000L || duration > 24 * 60 * 60 * 1000L) {
            throw Exception("Max duration must be between 1 minute and 1 hour")
        }

        if (duration < intervalDuration) {
            throw Exception("Max duration must be greater than interval duration")
        }

        return copy(maxDuration = duration)
    }

    fun setForceExactMaxDuration(forceExactMaxDuration: Boolean): AudioRecorderSettings {
        return copy(forceExactMaxDuration = forceExactMaxDuration)
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
