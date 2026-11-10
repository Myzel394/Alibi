package app.myzel394.alibi.recording

import androidx.test.platform.app.InstrumentationRegistry
import app.myzel394.alibi.helpers.MediaConverter
import com.antonkarpenko.ffmpegkit.FFmpegKit
import com.antonkarpenko.ffmpegkit.FFprobeKit
import com.antonkarpenko.ffmpegkit.ReturnCode
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class RecordingMergeTest {
    @Test fun videoFragmentsMergeIntoPlayableVideo() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val folder = File(context.cacheDir, "merge_video_${System.nanoTime()}").apply { mkdirs() }
        try {
            val fragments = listOf("red", "blue").mapIndexed { index, color ->
                File(folder, "$index.mp4").also {
                    val session = FFmpegKit.execute("-y -f lavfi -i color=c=$color:s=64x64:r=10 -t 0.4 -c:v mpeg4 -an ${it.absolutePath}")
                    assertTrue(session.allLogsAsString, ReturnCode.isSuccess(session.returnCode))
                }.absolutePath
            }
            val output = File(folder, "merged.mp4")
            withTimeout(30000) { MediaConverter.concatenateVideoFiles(fragments, output.absolutePath, " -c copy").await() }
            assertTrue(output.length() > 0)
            val information = FFprobeKit.getMediaInformation(output.absolutePath).mediaInformation
            assertNotNull(information)
            assertTrue(information.duration.toDouble() >= 0.7)
        } finally { folder.deleteRecursively() }
    }
    @Test fun audioFragmentsMergeIntoPlayableAudio() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val folder = File(context.cacheDir, "merge_audio_${System.nanoTime()}").apply { mkdirs() }
        try {
            val fragments = (0..1).map { index ->
                File(folder, "$index.aac").also {
                    val session = FFmpegKit.execute("-y -f lavfi -i sine=frequency=440:sample_rate=44100 -t 0.4 -c:a aac -f adts ${it.absolutePath}")
                    assertTrue(session.allLogsAsString, ReturnCode.isSuccess(session.returnCode))
                }.absolutePath
            }
            val output = File(folder, "merged.aac")
            withTimeout(30000) { MediaConverter.concatenateAudioFiles(fragments, output.absolutePath, " -c copy").await() }
            assertTrue(output.length() > 0)
            val information = FFprobeKit.getMediaInformation(output.absolutePath).mediaInformation
            assertNotNull(information)
            assertTrue(information.duration.toDouble() >= 0.7)
        } finally { folder.deleteRecursively() }
    }
}
