package app.myzel394.alibi.recording

import androidx.test.platform.app.InstrumentationRegistry
import app.myzel394.alibi.helpers.AudioBatchesFolder
import app.myzel394.alibi.helpers.BatchesFolder
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class RecordingStorageTest {
    @Test fun newSessionArchivesFragmentsAndPreservesMergedFiles() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val folder = AudioBatchesFolder(context, BatchesFolder.BatchType.INTERNAL,
            subfolderName = ".recording_test_${System.nanoTime()}")
        val directory = folder.getInternalFolder()
        try {
            folder.initFolders()
            File(directory, "1.aac").writeText("previous session")
            val merged = File(directory, "2026-11-10T10-00-00.aac").apply { writeText("export") }
            RecordingLibrary.archiveFragments(folder)
            assertFalse(File(directory, "1.aac").exists())
            assertTrue(directory.listFiles()!!.any { it.name.startsWith("fragment_") })
            assertEquals("export", merged.readText())
            File(directory, "1.aac").writeText("current session")
            assertEquals(1, folder.getBatchesForFFmpeg().size)
            folder.deleteRecordings()
            assertEquals(0, folder.getBatchesForFFmpeg().size)
            assertTrue(merged.exists())
            assertTrue(directory.listFiles()!!.any { it.name.startsWith("fragment_") })
        } finally { directory.deleteRecursively() }
    }
}
