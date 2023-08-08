package app.myzel394.alibi.services

import android.os.Handler
import android.os.Looper
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

abstract class ExtraRecorderInformationService: RecorderService() {
    abstract fun getAmplitudeAmount(): Int
    abstract fun getAmplitude(): Int

    private var recordingTime = 0L
    private lateinit var recordingTimeTimer: ScheduledExecutorService

    var amplitudes = mutableListOf<Int>()
        private set
    private lateinit var amplitudesTimer: Timer

    private val handler = Handler(Looper.getMainLooper())

    var onRecordingTimeChange: ((Long) -> Unit)? = null
    var onAmplitudeChange: ((List<Int>) -> Unit)? = null

    private fun createRecordingTimeTimer() {
        recordingTimeTimer = Executors.newSingleThreadScheduledExecutor().also {
            it.scheduleAtFixedRate(
                {
                    recordingTime += 1000
                },
                0,
                1000,
                TimeUnit.MILLISECONDS
            )
        }
    }

    private fun updateAmplitude() {
        amplitudes.add(getAmplitude())

        // Delete old amplitudes
        if (amplitudes.size > getAmplitudeAmount()) {
            amplitudes.drop(amplitudes.size - getAmplitudeAmount())
        }

        handler.postDelayed(::updateAmplitude, 100)
    }

    private fun createAmplitudesTimer() {
        amplitudesTimer = Timer().also {
            it.scheduleAtFixedRate(
                object: TimerTask() {
                    override fun run() {
                        updateAmplitude()
                    }
                },
                0,
                100,
            )
        }
    }

    override fun start() {
        createRecordingTimeTimer()
        createAmplitudesTimer()
    }

    override fun pause() {
        recordingTimeTimer.shutdown()
        amplitudesTimer.cancel()
    }

    override fun resume() {
        createRecordingTimeTimer()
        createAmplitudesTimer()
    }

    override fun stop() {
        recordingTimeTimer.shutdown()
        amplitudesTimer.cancel()
    }

}