package app.myzel394.alibi.services

import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.helpers.BatchesFolder
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

abstract class IntervalRecorderService<I, B : BatchesFolder> :
    RecorderService() {
    protected var counter = 0L
        private set

    lateinit var settings: AppSettings

    private lateinit var cycleTimer: ScheduledExecutorService

    abstract var batchesFolder: B

    var onBatchesFolderNotAccessible: () -> Unit = {}

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
        super.start()

        batchesFolder.initFolders()

        if (!batchesFolder.checkIfFolderIsAccessible()) {
            onBatchesFolderNotAccessible()
        }

        createTimer()
    }

    override fun pause() {
        super.pause()
        cycleTimer.shutdown()
    }

    override fun resume() {
        super.resume()
        createTimer()
    }

    override suspend fun stop() {
        cycleTimer.shutdown()
        batchesFolder.cleanup()
        super.stop()
    }

    fun clearAllRecordings() {
        batchesFolder.deleteRecordings()
    }

    private fun deleteOldRecordings() {
        val timeMultiplier = settings.maxDuration / settings.intervalDuration
        val earliestCounter = counter - timeMultiplier

        if (earliestCounter <= 0) {
            return
        }

        batchesFolder.deleteOldRecordings(earliestCounter)
    }
}