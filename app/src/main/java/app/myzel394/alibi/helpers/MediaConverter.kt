package app.myzel394.alibi.helpers

import android.content.Context
import android.util.Log
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import kotlinx.coroutines.CompletableDeferred
import java.io.File
import java.util.UUID

class MediaConverter {
    companion object {
        fun concatenateAudioFiles(
            inputFiles: Iterable<String>,
            outputFile: String,
            extraCommand: String = "",
        ): CompletableDeferred<Unit> {
            val completer = CompletableDeferred<Unit>()

            val filePathsConcatenated = inputFiles.joinToString("|")
            val command =
                "-protocol_whitelist saf,concat,content,file,subfile" +
                        " -i 'concat:$filePathsConcatenated'" +
                        " -y" +
                        " -acodec copy" +
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

        private fun createTempFile(content: String): File {
            val name = UUID.randomUUID().toString()

            return File.createTempFile("temp-$name", ".txt").apply {
                writeText(content)
            }
        }

        fun concatenateVideoFiles(
            inputFiles: Iterable<String>,
            outputFile: String,
            extraCommand: String = "",
        ): CompletableDeferred<Unit> {
            val completer = CompletableDeferred<Unit>()

            val listFile = createTempFile(inputFiles.joinToString("\n", prefix = "file "))

            val command =
                " -f concat" +
                        " -y" +
                        " -safe 0" +
                        " -i ${listFile.absolutePath}" +
                        " -c copy" +
                        extraCommand +
                        " $outputFile"

            FFmpegKit.executeAsync(
                command
            ) { session ->
                if (!ReturnCode.isSuccess(session!!.returnCode)) {
                    Log.d(
                        "Video Concatenation",
                        String.format(
                            "Command failed with state %s and rc %s.%s",
                            session.state,
                            session.returnCode,
                            session.failStackTrace,
                        )
                    )

                    completer.completeExceptionally(Exception("Failed to concatenate videos"))
                } else {
                    completer.complete(Unit)
                }
            }

            return completer
        }
    }
}