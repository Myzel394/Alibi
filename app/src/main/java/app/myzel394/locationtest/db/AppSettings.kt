package app.myzel394.locationtest.db

import android.media.MediaRecorder
import android.os.Build
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

@Serializable
data class AppSettings(
    val audioRecorderSettings: AudioRecorderSettings = AudioRecorderSettings(),
    val showAdvancedSettings: Boolean = false,
) {
    fun setShowAdvancedSettings(showAdvancedSettings: Boolean): AppSettings {
        return copy(showAdvancedSettings = showAdvancedSettings)
    }

    fun setAudioRecorderSettings(audioRecorderSettings: AudioRecorderSettings): AppSettings {
        return copy(audioRecorderSettings = audioRecorderSettings)
    }

    companion object {
        fun getDefaultInstance(): AppSettings = AppSettings()
    }
}

@Serializable
data class AudioRecorderSettings(
    // 60 seconds
    val intervalDuration: Long = 60 * 1000L,
    // 320 Kbps
    val bitRate: Int = 320000,
    val samplingRate: Int? = null,
    val outputFormat: Int? = null,
    val encoder: Int? = null,
) {
    fun getOutputFormat(): Int = outputFormat ?:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            MediaRecorder.OutputFormat.AAC_ADTS
        else
            MediaRecorder.OutputFormat.THREE_GPP

    fun getSamplingRate(): Int = samplingRate ?: when(getOutputFormat()) {
        MediaRecorder.OutputFormat.AAC_ADTS -> 96000
        MediaRecorder.OutputFormat.THREE_GPP -> 44100
        else -> throw Exception("Unknown output format")
    }

    fun getEncoder(): Int = encoder ?:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            MediaRecorder.AudioEncoder.AAC
        else
            MediaRecorder.AudioEncoder.AMR_NB

    fun getFileExtensions(): String =
        when(getOutputFormat()) {
            MediaRecorder.OutputFormat.AAC_ADTS -> "aac"
            MediaRecorder.OutputFormat.THREE_GPP -> "3gp"
            else -> throw Exception("Unknown output format")
        }

    fun setIntervalDuration(duration: Long): AudioRecorderSettings {
        if (duration < 10 * 1000L) {
            throw Exception("Interval duration must be at least 10 seconds")
        }

        return copy(intervalDuration = duration)
    }

    fun setBitRate(bitRate: Int): AudioRecorderSettings {
        if (bitRate < 1000) {
            throw Exception("Bit rate must be at least 1000")
        }

        return copy(bitRate = bitRate)
    }

    fun setSamplingRate(samplingRate: Int): AudioRecorderSettings {
        if (samplingRate < 1000) {
            throw Exception("Sampling rate must be at least 1000")
        }

        return copy(samplingRate = samplingRate)
    }

    fun setOutputFormat(outputFormat: Int): AudioRecorderSettings {
        if (outputFormat < 0 || outputFormat > 11) {
            throw Exception("OutputFormat is not a MediaRecorder.OutputFormat constant")
        }

        return copy(outputFormat = outputFormat)
    }

    fun setEncoder(encoder: Int): AudioRecorderSettings {
        if (encoder < 0 || encoder > 7) {
            throw Exception("Encoder is not a MediaRecorder.AudioEncoder constant")
        }

        return copy(encoder = encoder)
    }
}
