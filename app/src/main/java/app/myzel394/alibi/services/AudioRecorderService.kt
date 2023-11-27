package app.myzel394.alibi.services

import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.MediaRecorder
import android.media.MediaRecorder.OnErrorListener
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.compose.material3.SnackbarDuration
import androidx.documentfile.provider.DocumentFile
import app.myzel394.alibi.db.AudioRecorderSettings
import app.myzel394.alibi.db.RecordingInformation
import app.myzel394.alibi.enums.RecorderState
import app.myzel394.alibi.helpers.BatchesFolder
import app.myzel394.alibi.ui.utils.MicrophoneInfo
import java.lang.IllegalStateException

class AudioRecorderService :
    IntervalRecorderService<AudioRecorderService.Settings, RecordingInformation>() {
    var amplitudesAmount = 1000
    var selectedMicrophone: MicrophoneInfo? = null

    var recorder: MediaRecorder? = null
        private set
    var onSelectedMicrophoneChange: (MicrophoneInfo?) -> Unit = {}
    var onMicrophoneDisconnected: () -> Unit = {}
    var onMicrophoneReconnected: () -> Unit = {}

    var amplitudes = mutableListOf<Int>()
        private set

    private val handler = Handler(Looper.getMainLooper())

    var onAmplitudeChange: ((List<Int>) -> Unit)? = null

    private fun updateAmplitude() {
        if (state !== RecorderState.RECORDING) {
            return
        }

        amplitudes.add(getAmplitude())
        onAmplitudeChange?.invoke(amplitudes)

        // Delete old amplitudes
        if (amplitudes.size > getAmplitudeAmount()) {
            // Should be more efficient than dropping the elements, getting a new list
            // clearing old list and adding new elements to it
            repeat(amplitudes.size - getAmplitudeAmount()) {
                amplitudes.removeAt(0)
            }
        }

        handler.postDelayed(::updateAmplitude, 100)
    }

    private fun createAmplitudesTimer() {
        handler.postDelayed(::updateAmplitude, 100)
    }

    /// Tell Android to use the correct bluetooth microphone, if any selected
    private fun startAudioDevice() {
        if (selectedMicrophone == null) {
            return
        }

        val audioManger = getSystemService(AUDIO_SERVICE)!! as AudioManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            audioManger.setCommunicationDevice(selectedMicrophone!!.deviceInfo)
        } else {
            audioManger.startBluetoothSco()
        }
    }

    private fun clearAudioDevice() {
        val audioManger = getSystemService(AUDIO_SERVICE)!! as AudioManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            audioManger.clearCommunicationDevice()
        } else {
            audioManger.stopBluetoothSco()
        }
    }

    private fun createRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(this)
        } else {
            MediaRecorder()
        }.apply {
            // Audio Source is kinda strange, here are my experimental findings using a Pixel 7 Pro
            // and Redmi Buds 3 Pro:
            // - MIC: Uses the bottom microphone of the phone (17)
            // - CAMCORDER: Uses the top microphone of the phone (2)
            // - VOICE_COMMUNICATION: Uses the bottom microphone of the phone (17)
            // - DEFAULT: Uses the bottom microphone of the phone (17)
            setAudioSource(MediaRecorder.AudioSource.MIC)

            when (batchesFolder.type) {
                BatchesFolder.BatchType.INTERNAL -> {
                    setOutputFile(
                        batchesFolder.asInternalGetOutputPath(counter, settings.fileExtension)
                    )
                }

                BatchesFolder.BatchType.CUSTOM -> {
                    setOutputFile(
                        batchesFolder.asCustomGetFileDescriptor(counter, settings.fileExtension)
                    )
                }
            }

            setOutputFormat(settings.outputFormat)

            setAudioEncoder(settings.encoder)
            setAudioEncodingBitRate(settings.bitRate)
            setAudioSamplingRate(settings.samplingRate)
            setOnErrorListener(OnErrorListener { _, _, _ ->
                onError()
            })
        }
    }

    private fun resetRecorder() {
        runCatching {
            recorder?.let {
                it.stop()
                it.release()
            }
            clearAudioDevice()
            batchesFolder.cleanup()
        }
    }

    override fun getRecordingInformation() = RecordingInformation(
        folderPath = batchesFolder.exportFolderForSettings(),
        recordingStart = recordingStart,
        maxDuration = settings.maxDuration,
        fileExtension = settings.fileExtension,
        intervalDuration = settings.intervalDuration,
        type = RecordingInformation.Type.AUDIO,
    )

    override fun startNewCycle() {
        super.startNewCycle()

        val newRecorder = createRecorder().also {
            it.prepare()
        }

        resetRecorder()
        startAudioDevice()

        try {
            recorder = newRecorder
            newRecorder.start()
        } catch (error: RuntimeException) {
            onError()
        }
    }

    override fun start() {
        super.start()

        createAmplitudesTimer()
        registerMicrophoneListener()
    }

    override fun pause() {
        super.pause()

        resetRecorder()
    }

    override fun stop() {
        super.stop()

        resetRecorder()
        selectedMicrophone = null
        unregisterMicrophoneListener()
    }

    override fun resume() {
        super.resume()
        createAmplitudesTimer()
    }

    private fun getAmplitudeAmount(): Int = amplitudesAmount

    private fun getAmplitude(): Int {
        return try {
            recorder!!.maxAmplitude
        } catch (error: IllegalStateException) {
            0
        } catch (error: RuntimeException) {
            0
        }
    }

    fun changeMicrophone(microphone: MicrophoneInfo?) {
        selectedMicrophone = microphone
        onSelectedMicrophoneChange(microphone)

        if (state == RecorderState.RECORDING) {
            startNewCycle()
        }
    }

    private val audioDeviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
            super.onAudioDevicesAdded(addedDevices)

            if (selectedMicrophone == null) {
                return
            }

            // We can't compare the ID, as it seems to be changing on each reconnect
            val newDevice = addedDevices?.find {
                it.productName == selectedMicrophone!!.deviceInfo.productName &&
                        it.isSink == selectedMicrophone!!.deviceInfo.isSink &&
                        it.type == selectedMicrophone!!.deviceInfo.type && (
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            it.address == selectedMicrophone!!.deviceInfo.address
                        } else true
                        )
            }
            if (newDevice != null) {
                changeMicrophone(MicrophoneInfo.fromDeviceInfo(newDevice))

                onMicrophoneReconnected()
            }
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
            super.onAudioDevicesRemoved(removedDevices)

            if (selectedMicrophone == null) {
                return
            }

            if (removedDevices?.find { it.id == selectedMicrophone!!.deviceInfo.id } != null) {
                onMicrophoneDisconnected()
            }
        }
    }

    private fun registerMicrophoneListener() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE)!! as AudioManager

        audioManager.registerAudioDeviceCallback(
            audioDeviceCallback,
            Handler(Looper.getMainLooper())
        )
    }

    private fun unregisterMicrophoneListener() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE)!! as AudioManager

        audioManager.unregisterAudioDeviceCallback(audioDeviceCallback)
    }

    data class Settings(
        override val maxDuration: Long,
        override val intervalDuration: Long,
        val bitRate: Int,
        val samplingRate: Int,
        val outputFormat: Int,
        val encoder: Int,
        val folder: String? = null,
    ) : IntervalRecorderService.Settings(
        maxDuration = maxDuration,
        intervalDuration = intervalDuration
    ) {
        val fileExtension: String
            get() = when (outputFormat) {
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
            fun from(audioRecorderSettings: AudioRecorderSettings): Settings {
                return Settings(
                    intervalDuration = audioRecorderSettings.intervalDuration,
                    bitRate = audioRecorderSettings.bitRate,
                    samplingRate = audioRecorderSettings.getSamplingRate(),
                    outputFormat = audioRecorderSettings.getOutputFormat(),
                    encoder = audioRecorderSettings.getEncoder(),
                    maxDuration = audioRecorderSettings.maxDuration,
                )
            }
        }
    }
}