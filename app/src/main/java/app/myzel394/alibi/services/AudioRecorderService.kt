package app.myzel394.alibi.services

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.MediaRecorder
import android.media.MediaRecorder.OnErrorListener
import android.media.MediaRecorder.getAudioSourceMax
import android.os.Build
import java.lang.IllegalStateException

class AudioRecorderService: IntervalRecorderService() {
    var amplitudesAmount = 1000
    var selectedDevice: AudioDeviceInfo? = null

    var recorder: MediaRecorder? = null
        private set
    var onError: () -> Unit = {}

    val filePath: String
        get() = "$folder/$counter.${settings!!.fileExtension}"

    private fun _setAudioDevice() {
        val audioManger = getSystemService(AUDIO_SERVICE)!! as AudioManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (selectedDevice == null) {
                audioManger.clearCommunicationDevice()
        } else {
            audioManger.setCommunicationDevice(selectedDevice!!)
        }
        } else {
            if (selectedDevice == null) {
                audioManger.stopBluetoothSco()
            } else {
                audioManger.startBluetoothSco()
            }
        }

    }

    private fun createRecorder(): MediaRecorder {
        _setAudioDevice()

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
            setOutputFile(filePath)
            setOutputFormat(settings!!.outputFormat)
            setAudioEncoder(settings!!.encoder)
            setAudioEncodingBitRate(settings!!.bitRate)
            setAudioSamplingRate(settings!!.samplingRate)
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
        }
    }

    override fun startNewCycle() {
        super.startNewCycle()

        val newRecorder = createRecorder().also {
            it.prepare()
        }

        resetRecorder()

        try {
            recorder = newRecorder
            newRecorder.start()
        } catch (error: RuntimeException) {
            onError()
        }
    }

    override fun pause() {
        super.pause()

        resetRecorder()
    }

    override fun stop() {
        super.stop()

        resetRecorder()
    }

    override fun getAmplitudeAmount(): Int = amplitudesAmount

    override fun getAmplitude(): Int {
        return try {
            recorder!!.maxAmplitude
        } catch (error: IllegalStateException) {
            0
        } catch (error: RuntimeException) {
            0
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "changeAudioDevice") {
            selectedDevice = intent.getStringExtra("deviceID")!!.let {
                if (it == "null") {
                    null
                } else {
                    val audioManager = getSystemService(AUDIO_SERVICE)!! as AudioManager
                    audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS).find { device ->
                        device.id == it.toInt()
                    }
                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    companion object {
        fun changeAudioDevice(deviceID: String?, context: Context) {
            val intent = Intent("changeAudioDevice").apply {
                putExtra("deviceID", deviceID ?: "null")
            }
            context.startService(intent)
        }
    }
}