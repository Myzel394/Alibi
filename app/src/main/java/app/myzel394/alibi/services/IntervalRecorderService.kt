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

    // Tracks the index of the currently locked file
    private var lockedIndex: Long? = null

    lateinit var settings: AppSettings

    private lateinit var cycleTimer: ScheduledExecutorService

    abstract var batchesFolder: B

    var onBatchesFolderNotAccessible: () -> Unit = {}

    abstract fun getRecordingInformation(): I

    // When saving the recording, the files should be locked.
    // This prevents the service from deleting the currently available files, so that
    // they can be safely used to save the recording.
    // Once finished, make sure to unlock the files using `unlockFiles`.
    fun lockFiles() {
        lockedIndex = counter
    }

    // Unlocks and deletes the files that were locked using `lockFiles`.
    fun unlockFiles(cleanupFiles: Boolean = false) {
        if (cleanupFiles) {
            batchesFolder.deleteRecordings(0..<lockedIndex!!)
        }

        lockedIndex = null
    }

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

            throw AvoidErrorDialogError()
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
        val earliestCounter = Math.max(counter - timeMultiplier, lockedIndex ?: 0)

        if (earliestCounter <= 0) {
            return
        }

        batchesFolder.deleteRecordings(0..earliestCounter)
    }
}