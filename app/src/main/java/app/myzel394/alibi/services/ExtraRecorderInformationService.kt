package app.myzel394.alibi.services

import android.os.Handler
import android.os.Looper
import app.myzel394.alibi.enums.RecorderState
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

abstract class ExtraRecorderInformationService: RecorderService() {
    abstract fun getAmplitudeAmount(): Int
    abstract fun getAmplitude(): Int

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
            amplitudes.drop(amplitudes.size - getAmplitudeAmount())
        }

        handler.postDelayed(::updateAmplitude, 100)
    }

    private fun createAmplitudesTimer() {
        handler.postDelayed(::updateAmplitude, 100)
    }

    override fun start() {
        createAmplitudesTimer()
    }

    override fun resume() {
        createAmplitudesTimer()
    }

}