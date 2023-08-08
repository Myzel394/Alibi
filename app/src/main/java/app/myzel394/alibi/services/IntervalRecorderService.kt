package app.myzel394.alibi.services

import android.media.MediaRecorder
import app.myzel394.alibi.db.AudioRecorderSettings
import app.myzel394.alibi.db.LastRecording
import java.io.File
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

abstract class IntervalRecorderService: ExtraRecorderInformationService() {
    protected var counter = 0
        private set
    protected lateinit var folder: File
    lateinit var settings: Settings
        protected set

    private lateinit var cycleTimer: ScheduledExecutorService

    fun createLastRecording(): LastRecording = LastRecording(
        folderPath = folder.absolutePath,
        recordingStart = recordingStart,
        maxDuration = settings.maxDuration,
        fileExtension = settings.fileExtension,
        intervalDuration = settings.intervalDuration,
        forceExactMaxDuration = settings.forceExactMaxDuration,
    )

    // Make overrideable
    open fun startNewCycle() {
        deleteOldRecordings()
    }

    private fun createTimer() {
        cycleTimer = Executors.newSingleThreadScheduledExecutor().also {
            it.scheduleAtFixedRate(
                {
                    startNewCycle()
                },
                0,
                settings.intervalDuration,
                TimeUnit.MILLISECONDS
            )
        }
    }

    override fun start() {
        folder.mkdirs()

        createTimer()
    }

    override fun pause() {
        cycleTimer.shutdown()
    }

    override fun resume() {
        createTimer()
    }

    override fun stop() {
        cycleTimer.shutdown()
    }

    private fun deleteOldRecordings() {
        val timeMultiplier = settings.maxDuration / settings.intervalDuration
        val earliestCounter = counter - timeMultiplier

        folder.listFiles()?.forEach { file ->
            val fileCounter = file.nameWithoutExtension.toIntOrNull() ?: return

            if (fileCounter < earliestCounter) {
                file.delete()
            }
        }
    }

    data class Settings(
        val maxDuration: Long,
        val intervalDuration: Long,
        val forceExactMaxDuration: Boolean,
        val bitRate: Int,
        val samplingRate: Int,
        val outputFormat: Int,
        val encoder: Int,
    ) {
        val fileExtension: String
            get() = when(outputFormat) {
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
                    forceExactMaxDuration = audioRecorderSettings.forceExactMaxDuration,
                )
            }
        }
    }
}