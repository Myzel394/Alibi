package app.myzel394.alibi.ui.utils

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build

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

        fun fetchDeviceMicrophones(context: Context): List<MicrophoneInfo> {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE)!! as AudioManager
            val mics =
                audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS).map(::fromDeviceInfo)
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                audioManager.availableCommunicationDevices.let {
                    it.subList(2, it.size)
                }.map(::fromDeviceInfo)
            } else {
                audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS).let {
                    it.slice(1 until it.size)
                }.map(::fromDeviceInfo)
            }
        }
    }


    enum class MicrophoneType {
        BLUETOOTH,
        WIRED,
        PHONE,
        OTHER,
    }
}