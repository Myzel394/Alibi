package app.myzel394.alibi.ui.utils

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.util.Log

val ALLOWED_MICROPHONE_TYPES =
    setOf(
        AudioDeviceInfo.TYPE_BLUETOOTH_SCO,
        AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,
        AudioDeviceInfo.TYPE_USB_DEVICE,
        AudioDeviceInfo.TYPE_USB_ACCESSORY,
        AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
        AudioDeviceInfo.TYPE_IP,
        AudioDeviceInfo.TYPE_DOCK,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            AudioDeviceInfo.TYPE_DOCK_ANALOG
        } else {
        },
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AudioDeviceInfo.TYPE_BLE_HEADSET
        } else {
        },
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AudioDeviceInfo.TYPE_REMOTE_SUBMIX
        } else {
        },
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AudioDeviceInfo.TYPE_USB_HEADSET
        } else {
        },
    )

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
            return try {
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE)!! as AudioManager
                (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    audioManager.availableCommunicationDevices.map(::fromDeviceInfo)
                } else {
                    audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS).map(::fromDeviceInfo)
                }).filter {
                    ALLOWED_MICROPHONE_TYPES.contains(it.deviceInfo.type) && it.deviceInfo.isSink
                }
            } catch (error: Exception) {
                Log.getStackTraceString(error)

                emptyList()
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