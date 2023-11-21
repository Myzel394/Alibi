package app.myzel394.alibi.services

import android.media.MediaRecorder
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.db.AudioRecorderSettings
import app.myzel394.alibi.db.RecordingInformation
import app.myzel394.alibi.helpers.AudioRecorderExporter
import app.myzel394.alibi.helpers.BatchesFolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.w3c.dom.DocumentFragment
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

abstract class IntervalRecorderService : ExtraRecorderInformationService() {
    private var job = SupervisorJob()
    private var scope = CoroutineScope(Dispatchers.IO + job)

    protected var counter = 0L
        private set

    lateinit var settings: Settings

    private lateinit var cycleTimer: ScheduledExecutorService

    var batchesFolder: BatchesFolder = BatchesFolder.viaInternalFolder(this)

    var onCustomOutputFolderNotAccessible: () -> Unit = {}

    fun getRecordingInformation(): RecordingInformation = RecordingInformation(
        folderPath = batchesFolder.exportFolderForSettings(),
        recordingStart = recordingStart,
        maxDuration = settings.maxDuration,
        fileExtension = settings.fileExtension,
        intervalDuration = settings.intervalDuration,
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
                settings.intervalDuration,
                TimeUnit.MILLISECONDS
            )
        }
    }

    override fun start() {
        super.start()

        batchesFolder.initFolders()
        if (!batchesFolder.checkIfFolderIsAccessible()) {
            batchesFolder =
                BatchesFolder.viaInternalFolder(this@IntervalRecorderService)
            batchesFolder.initFolders()
            onCustomOutputFolderNotAccessible()
        }

        createTimer()
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

    fun clearAllRecordings() {
        batchesFolder.deleteRecordings()
    }

    private fun deleteOldRecordings() {
        val timeMultiplier = settings!!.maxDuration / settings!!.intervalDuration
        val earliestCounter = counter - timeMultiplier

        batchesFolder.deleteOldRecordings(earliestCounter)
    }

    data class Settings(
        val maxDuration: Long,
        val intervalDuration: Long,
        val bitRate: Int,
        val samplingRate: Int,
        val outputFormat: Int,
        val encoder: Int,
        val folder: String? = null,
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