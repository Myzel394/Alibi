package app.myzel394.alibi.recording

import org.junit.Assert.*
import org.junit.Test

class RecordingLibraryTest {
    @Test fun currentAndArchivedFragmentsAreDistinctFromMergedExports() {
        listOf("1.mp4", "42.aac", "fragment_1700000000_1.mp4", "alibi-recording-video_recordings-4.mp4").forEach {
            assertTrue(it, RecordingLibrary.isFragment(it))
        }
        assertFalse(RecordingLibrary.isFragment("2026-11-10T10-30-00.mp4"))
    }
    @Test fun customTreeRootOnlyAcceptsTimestampedMediaExports() {
        assertTrue(RecordingLibrary.isExportName("2026-11-10T10-30-00.mp4"))
        assertFalse(RecordingLibrary.isExportName("personal-photo.mp4"))
        assertTrue(RecordingLibrary.isMedia("2026-11-10T10-30-00.mp4"))
        assertFalse(RecordingLibrary.isMedia("2026-11-10T10-30-00.json"))
    }
    @Test fun sameContainerKeepsAudioAndVideoMimeTypesSeparate() {
        assertEquals("audio/mp4", RecordingLibrary.mime("1.mp4", false))
        assertEquals("video/mp4", RecordingLibrary.mime("1.mp4", true))
        assertEquals("audio/aac", RecordingLibrary.mime("1.aac", false))
    }
}
