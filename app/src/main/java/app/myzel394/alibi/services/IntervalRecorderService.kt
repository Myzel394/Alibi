package app.myzel394.alibi.services

import android.content.Context
import android.media.MediaRecorder
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.db.AudioRecorderSettings
import app.myzel394.alibi.db.LastRecording
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime
import java.util.Timer
import java.util.TimerTask
import java.util.UUID
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

abstract class IntervalRecorderService: ExtraRecorderInformationService() {
    private var job = SupervisorJob()
    private var scope = CoroutineScope(Dispatchers.IO + job)

    protected var counter = 0
        private set
    protected lateinit var folder: File
    var settings: Settings? = null
        protected set

    private lateinit var cycleTimer: ScheduledExecutorService

    fun createLastRecording(): LastRecording = LastRecording(
        folderPath = folder.absolutePath,
        recordingStart = recordingStart,
        maxDuration = settings!!.maxDuration,
        fileExtension = settings!!.fileExtension,
        intervalDuration = settings!!.intervalDuration,
        forceExactMaxDuration = settings!!.forceExactMaxDuration,
    )

    // Make overrideable
    open fun startNewCycle() {
        counter += 1
        deleteOldRecordings()
    }

    private fun createTimer() {
        cycleTimer = Executors.newSingleThreadScheduledExecutor().also {
            it.scheduleAtFixedRate(
                {
                    startNewCycle()
                },
                0,
                settings!!.intervalDuration,
                TimeUnit.MILLISECONDS
            )
        }
    }

    private fun getRandomFileFolder(): String {
        // uuid
        val folder = UUID.randomUUID().toString()

        return "${externalCacheDir!!.absolutePath}/$folder"
    }

    override fun start() {
        super.start()

        folder = File(getRandomFileFolder())
        folder.mkdirs()

        scope.launch {
            dataStore.data.collectLatest { preferenceSettings ->
                if (settings == null) {
                    settings = Settings.from(preferenceSettings.audioRecorderSettings)

                    createTimer()
                }
            }
        }
    }

    override fun pause() {
        cycleTimer.shutdown()
    }

    override fun resume() {
        createTimer()

        // We first want to start our timers, so the `ExtraRecorderInformationService` can fetch
        // amplitudes
        super.resume()
    }

    override fun stop() {
        cycleTimer.shutdown()
    }

    private fun deleteOldRecordings() {
        val timeMultiplier = settings!!.maxDuration / settings!!.intervalDuration
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