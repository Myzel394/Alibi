package app.myzel394.alibi.services

import app.myzel394.alibi.helpers.BatchesFolder
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

abstract class IntervalRecorderService<S : IntervalRecorderService.Settings, I> :
    RecorderService() {
    protected var counter = 0L
        private set

    lateinit var settings: S

    private lateinit var cycleTimer: ScheduledExecutorService

    abstract var batchesFolder: BatchesFolder

    var onCustomOutputFolderNotAccessible: () -> Unit = {}

    abstract fun getRecordingInformation(): I

    // Make overrideable
    open fun startNewCycle() {
        counter += 1
        deleteOldRecordings()
    }

    private fun createTimer() {
        cycleTimer = Executors.newSingleThreadScheduledExecutor().also {
            it.scheduleAtFixedRate(
                ::startNewCycle,
                0,
                settings.intervalDuration,
                TimeUnit.MILLISECONDS
            )
        }
    }

    override fun start() {
        batchesFolder.initFolders()
        if (!batchesFolder.checkIfFolderIsAccessible()) {
            onCustomOutputFolderNotAccessible()
            return
        }

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

    fun clearAllRecordings() {
        batchesFolder.deleteRecordings()
    }

    private fun deleteOldRecordings() {
        val timeMultiplier = settings.maxDuration / settings.intervalDuration
        val earliestCounter = counter - timeMultiplier

        batchesFolder.deleteOldRecordings(earliestCounter)
    }

    abstract class Settings(
        open val maxDuration: Long,
        open val intervalDuration: Long,
    )
}