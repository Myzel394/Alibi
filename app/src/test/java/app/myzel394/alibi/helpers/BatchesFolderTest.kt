package app.myzel394.alibi.helpers

import android.content.Context
import android.net.Uri
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.db.RecordingInformation
import io.mockk.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.time.LocalDateTime
import kotlin.reflect.KFunction4

class BatchesFolderTest {

    private lateinit var mockContext: Context
    private lateinit var mockAppSettings: AppSettings
    private lateinit var mockRecordingInformation: RecordingInformation
    private lateinit var mockCustomFolder: Uri // Using Uri for DocumentFile simplicity in mock
    private lateinit var concreteBatchesFolder: ConcreteBatchesFolder

    // Mock implementation of BatchesFolder for testing
    class ConcreteBatchesFolder(
        context: Context,
        type: BatchType,
        customFolderUri: Uri? = null, // Changed to Uri for easier mocking
        subfolderName: String = "test_subfolder"
    ) : BatchesFolder(
        context,
        type,
        customFolderUri?.let { mockk<DocumentFile>().apply { every { uri } returns it } }, // Mock DocumentFile
        subfolderName
    ) {
        var concatenateCalled = false
        var getOutputFileForFFmpegCalled = false

        // Simplified mock for concatenation function
        override val concatenationFunction: KFunction4<Iterable<String>, String, String, (Int) -> Unit, CompletableDeferred<Unit>> =
            mockk<(Iterable<String>, String, String, (Int) -> Unit) -> CompletableDeferred<Unit>>().apply {
                every { this@apply.invoke(any(), any(), any(), any()) } answers {
                    concatenateCalled = true
                    CompletableDeferred(Unit) // Simulate successful FFmpeg run
                }
            }

        override val ffmpegParameters: Array<String> = arrayOf(" -c copy") // Dummy parameter
        override val scopedMediaContentUri: Uri = mockk()
        override val legacyMediaFolder: File = mockk<File>().apply {
            every { mkdirs() } returns true
            every { exists() } returns true // Assume it exists for checkIfOutputAlreadyExists
        }

        override fun getOutputFileForFFmpeg(
            date: LocalDateTime,
            extension: String,
            fileName: String
        ): String {
            getOutputFileForFFmpegCalled = true
            return "mock/output/path/$fileName"
        }

        override fun cleanup() {
            // No-op for tests
        }

        // Helper to reset flags
        fun resetFlags() {
            concatenateCalled = false
            getOutputFileForFFmpegCalled = false
        }

        // Override methods that would interact with file system or FFmpegKit if necessary for deeper tests
        override fun getBatchesForFFmpeg(): List<String> {
            return listOf("mock/batch1.mp4", "mock/batch2.mp4") // Dummy batch files
        }

        override fun checkIfOutputAlreadyExists(fileName: String): Boolean {
            return false // Assume output does not exist to force concatenation logic
        }
    }

    @Before
    fun setUp() {
        mockkStatic(Log::class) // Mock Android's Log class
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0


        mockContext = mockk(relaxed = true)
        mockAppSettings = mockk(relaxed = true)
        mockRecordingInformation = mockk(relaxed = true)
        mockCustomFolder = mockk(relaxed = true) // Mock Uri

        // Default stubs for mocks
        every { mockRecordingInformation.recordingStart } returns LocalDateTime.now()
        every { mockRecordingInformation.fileExtension } returns "mp4"
        every { mockRecordingInformation.getFullDuration() } returns 60000L // 1 minute
        every { mockRecordingInformation.getStartDateForFilename(any()) } returns LocalDateTime.now()

        // Initialize with BatchType.INTERNAL for simplicity, can be changed per test
        concreteBatchesFolder = ConcreteBatchesFolder(mockContext, BatchesFolder.BatchType.INTERNAL)
        concreteBatchesFolder.resetFlags() // Ensure flags are reset before each test
    }

    @After
    fun tearDown() {
        unmockkAll() // Unmock all static mocks and regular mocks
    }

    @Test
    fun `concatenate should be called when combine_batches is true`() = runBlocking {
        every { mockAppSettings.combine_batches } returns true
        every { mockAppSettings.filenameFormat } returns AppSettings.FilenameFormat.DATETIME_NOW

        val resultPath = concreteBatchesFolder.concatenate(
            recording = mockRecordingInformation,
            appSettings = mockAppSettings,
            fileName = "test_output.mp4"
        )

        assertTrue("Concatenation function should be called", concreteBatchesFolder.concatenateCalled)
        assertTrue("getOutputFileForFFmpeg should be called", concreteBatchesFolder.getOutputFileForFFmpegCalled)
        assertEquals("mock/output/path/test_output.mp4", resultPath)
    }

    @Test
    fun `concatenate should be skipped when combine_batches is false`() = runBlocking {
        every { mockAppSettings.combine_batches } returns false
        every { mockAppSettings.filenameFormat } returns AppSettings.FilenameFormat.DATETIME_NOW

        val resultPath = concreteBatchesFolder.concatenate(
            recording = mockRecordingInformation,
            appSettings = mockAppSettings,
            fileName = "test_output.mp4"
        )

        assertFalse("Concatenation function should NOT be called", concreteBatchesFolder.concatenateCalled)
        // getOutputFileForFFmpeg might still be called depending on implementation details when skipping
        // but the primary assertion is that the core concatenation work is skipped.
        // Based on current implementation, it returns "" before calling getOutputFileForFFmpeg in the main flow.
        assertFalse("getOutputFileForFFmpeg should NOT be called if concatenation is skipped early", concreteBatchesFolder.getOutputFileForFFmpegCalled)
        assertEquals("Result path should be empty when concatenation is skipped", "", resultPath)
    }

    // Example test for BatchType.CUSTOM to show how it could be handled
    @Test
    fun `concatenate with custom folder and combine_batches true`() = runBlocking {
        concreteBatchesFolder = ConcreteBatchesFolder(mockContext, BatchesFolder.BatchType.CUSTOM, mockCustomFolder)
        every { mockAppSettings.combine_batches } returns true
        every { mockAppSettings.filenameFormat } returns AppSettings.FilenameFormat.DATETIME_NOW

        // Mock DocumentFile interactions if getBatchesForFFmpeg for CUSTOM type relies on it
        val mockDocFile = mockk<DocumentFile>()
        every { mockDocFile.uri } returns mockCustomFolder
        every { mockDocFile.findFile(any()) } returns null // Assume file doesn't exist to avoid other paths
        every { concreteBatchesFolder.customFolder } returns mockDocFile


        // Mock FFmpegKitConfig for CUSTOM type if it's used directly in getBatchesForFFmpeg
        mockkStatic(FFmpegKitConfig::class)
        every { FFmpegKitConfig.getSafParameterForRead(any(), any()) } returns "saf://mockpath"


        val resultPath = concreteBatchesFolder.concatenate(
            recording = mockRecordingInformation,
            appSettings = mockAppSettings,
            fileName = "test_output_custom.mp4"
        )

        assertTrue("Concatenation function should be called for custom folder", concreteBatchesFolder.concatenateCalled)
        assertTrue("getOutputFileForFFmpeg should be called for custom folder", concreteBatchesFolder.getOutputFileForFFmpegCalled)
        assertEquals("mock/output/path/test_output_custom.mp4", resultPath)
    }
}
