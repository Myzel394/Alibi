package app.myzel394.alibi.helpers

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.arthenica.ffmpegkit.ReturnCode
import kotlinx.coroutines.CompletableDeferred
import java.io.File
import java.lang.Compiler.command
import java.util.UUID
import kotlin.math.log

// Abstract class for concatenating audio and video files
// The concatenator runs in its own thread to avoid unresponsiveness.
// You may be wondering why we simply not iterate over the FFMPEG_PARAMETERS
// in this thread and then call each FFmpeg initiation just right after it?
// The answer: It's easier; We don't have to deal with the `getBatchesForFFmpeg` function, because
// the batches are only usable once and we if iterate in this thread over the FFMPEG_PARAMETERS
// we would need to refetch the batches here, which is more messy.
// This is okay, because in 99% of the time the first or second parameter will work,
// and so there is no real performance loss.
abstract class Concatenator(
    private val inputFiles: Iterable<String>,
    private val outputFile: String,
    private val extraCommand: String
) : Thread() {
    abstract fun concatenate(): CompletableDeferred<Unit>

    class FFmpegException(message: String) : Exception(message)
}

data class AudioConcatenator(
    private val inputFiles: Iterable<String>,
    private val outputFile: String,
    private val extraCommand: String
) : Concatenator(
    inputFiles,
    outputFile,
    extraCommand
) {
    override fun concatenate(): CompletableDeferred<Unit> {
        val completer = CompletableDeferred<Unit>()

        val filePathsConcatenated = inputFiles.joinToString("|")
        val command =
            "-protocol_whitelist saf,concat,content,file,subfile" +
                    " -i 'concat:$filePathsConcatenated'" +
                    " -y" +
                    extraCommand +
                    " $outputFile"

        FFmpegKit.executeAsync(
            command
        ) { session ->
            if (!ReturnCode.isSuccess(session!!.returnCode)) {
                Log.d(
                    "Audio Concatenation",
                    String.format(
                        "Command failed with state %s and rc %s.%s",
                        session.state,
                        session.returnCode,
                        session.failStackTrace,
                    )
                )

                completer.completeExceptionally(Exception("Failed to concatenate audios"))
            } else {
                completer.complete(Unit)
            }
        }

        return completer
    }
}


class MediaConverter {
    companion object {
        fun concatenateAudioFiles(
            inputFiles: Iterable<String>,
            outputFile: String,
            extraCommand: String = "",
            onProgress: (Int) -> Unit = { },
        ): CompletableDeferred<Unit> {
            val completer = CompletableDeferred<Unit>()

            val filePathsConcatenated = inputFiles.joinToString("|")
            val command =
                "-protocol_whitelist saf,concat,content,file,subfile" +
                        " -i 'concat:$filePathsConcatenated'" +
                        extraCommand +
                        " -nostats" +
                        " -loglevel error" +
                        " -y" +
                        " $outputFile"

            FFmpegKit.executeAsync(
                command,
                { session ->
                    if (!ReturnCode.isSuccess(session!!.returnCode)) {
                        Log.d(
                            "Audio Concatenation",
                            String.format(
                                "Command failed with state %s and rc %s.%s",
                                session.state,
                                session.returnCode,
                                session.failStackTrace,
                            )
                        )

                        completer.completeExceptionally(Exception("Failed to concatenate audios"))
                    } else {
                        completer.complete(Unit)
                    }
                },
                {},
                { statistics ->
                    onProgress(statistics.time)
                }
            )

            return completer
        }

        private fun createTempFile(content: String): File {
            val id = UUID.randomUUID().toString()

            return File.createTempFile(".temp-ffmpeg-files-$id", ".txt").apply {
                writeText(content)
            }
        }

        fun concatenateVideoFiles(
            inputFiles: Iterable<String>,
            outputFile: String,
            extraCommand: String = "",
            onProgress: (Int) -> Unit = { },
        ): CompletableDeferred<Unit> {
            val completer = CompletableDeferred<Unit>()

            val listFile = createTempFile(inputFiles.joinToString("\n") { "file '$it'" })

            val command =
                "-protocol_whitelist saf,concat,content,file,subfile" +
                        " -f concat" +
                        " -safe 0" +
                        " -i ${listFile.absolutePath}" +
                        extraCommand +
                        " -strict normal" +
                        " -nostats" +
                        " -loglevel error" +
                        " -y" +
                        " $outputFile"

            FFmpegKit.executeAsync(
                command,
                { session ->
                    runCatching {
                        listFile.delete()
                    }

                    if (ReturnCode.isSuccess(session!!.returnCode)) {
                        completer.complete(Unit)
                    } else {
                        Log.d(
                            "Video Concatenation",
                            String.format(
                                "Command failed with state %s and rc %s.%s",
                                session.state,
                                session.returnCode,
                                session.failStackTrace,
                            )
                        )

                        completer.completeExceptionally(FFmpegException("Failed to concatenate videos"))
                    }
                },
                {},
                { statistics ->
                    onProgress(statistics.time)
                }
            )

            return completer
        }
    }

    class FFmpegException(message: String) : Exception(message)
}