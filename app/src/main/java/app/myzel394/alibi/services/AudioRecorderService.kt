package app.myzel394.alibi.services

import android.media.MediaRecorder
import android.media.MediaRecorder.OnErrorListener
import android.os.Build
import java.lang.IllegalStateException

class AudioRecorderService: IntervalRecorderService() {
    var amplitudesAmount = 1000

    var recorder: MediaRecorder? = null
        private set
    var onError: () -> Unit = {}

    val filePath: String
        get() = "$folder/$counter.${settings!!.fileExtension}"

    private fun createRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(this)
        } else {
            MediaRecorder()
        }.apply {
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
        }
    }
}