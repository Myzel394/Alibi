package app.myzel394.alibi.ui.utils

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager

data class MicrophoneInfo(
    val deviceInfo: AudioDeviceInfo,
) {
    val name: String
        get() = deviceInfo.productName.toString()

    val type: MicrophoneType
        get() = when (deviceInfo.type) {
            AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> MicrophoneType.BLUETOOTH
            AudioDeviceInfo.TYPE_WIRED_HEADSET -> MicrophoneType.WIRED
            AudioDeviceInfo.TYPE_BUILTIN_MIC -> MicrophoneType.PHONE
            else -> MicrophoneType.OTHER
        }

    companion object {
        fun fromDeviceInfo(deviceInfo: AudioDeviceInfo): MicrophoneInfo {
            return MicrophoneInfo(deviceInfo)
        }

        @SuppressLint("NewApi")
        fun fetchDeviceMicrophones(context: Context): List<MicrophoneInfo> {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE)!! as AudioManager
            return audioManager.availableCommunicationDevices.let {
                it.subList(2, it.size)
            }.map(::fromDeviceInfo)
        }
    }


    enum class MicrophoneType {
        BLUETOOTH,
        WIRED,
        PHONE,
        OTHER,
    }
}