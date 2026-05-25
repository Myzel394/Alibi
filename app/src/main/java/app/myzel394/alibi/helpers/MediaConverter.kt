package app.myzel394.alibi.helpers

import android.util.Log
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import kotlinx.coroutines.CompletableDeferred
import java.io.File
import java.util.UUID

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
                Log.i(
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
        private const val MAX_FFMPEG_FAILURE_LOG_CHARS = 8000
        private const val FD_INPUT_PREFIX = "fd:"

        private fun String.tailForLog(): String =
            takeLast(MAX_FFMPEG_FAILURE_LOG_CHARS)

        private fun String.asFileDescriptorNumber(): Int? {
            if (!startsWith(FD_INPUT_PREFIX)) {
                return null
            }

            return removePrefix(FD_INPUT_PREFIX).toIntOrNull()
        }

        private fun asConcatFileEntry(inputFile: String): String {
            val fileDescriptor = inputFile.asFileDescriptorNumber()

            return if (fileDescriptor == null) {
                "file '$inputFile'"
            } else {
                "file 'fd:'\noption fd $fileDescriptor"
            }
        }

        private fun asFFmpegOutputFile(outputFile: String): String {
            val fileDescriptor = outputFile.asFileDescriptorNumber()

            return if (fileDescriptor == null) {
                outputFile
            } else {
                "-fd $fileDescriptor fd:"
            }
        }

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
                        " -strict normal" +
                        " -i 'concat:$filePathsConcatenated'" +
                        extraCommand +
                        " -y" +
                        " $outputFile"

            FFmpegKit.executeAsync(
                command,
                { session ->
                    if (!ReturnCode.isSuccess(session!!.returnCode)) {
                        Log.i(
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
                    onProgress(statistics.time.toInt())
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

            val listFile = createTempFile(inputFiles.joinToString("\n") { asConcatFileEntry(it) })

            val command =
                "-protocol_whitelist saf,concat,content,file,subfile,fd" +
                        " -safe 0" +
                        " -strict normal" +
                        " -f concat" +
                        " -i ${listFile.absolutePath}" +
                        extraCommand +
                        " -y" +
                        " ${asFFmpegOutputFile(outputFile)}"

            FFmpegKit.executeAsync(
                command,
                { session ->
                    runCatching {
                        listFile.delete()
                    }

                    if (ReturnCode.isSuccess(session!!.returnCode)) {
                        completer.complete(Unit)
                    } else {
                        Log.i(
                            "Video Concatenation",
                            String.format(
                                "Command failed with state %s and rc %s.%s\n%s",
                                session.state,
                                session.returnCode,
                                session.failStackTrace,
                                session.allLogsAsString.tailForLog(),
                            )
                        )

                        completer.completeExceptionally(FFmpegException("Failed to concatenate videos"))
                    }
                },
                {},
                { statistics ->
                    onProgress(statistics.time.toInt())
                }
            )

            return completer
        }
    }

    class FFmpegException(message: String) : Exception(message)
}