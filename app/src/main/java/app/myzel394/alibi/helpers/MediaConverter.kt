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
            val id = UUID.randomUUID().toString()

            return File.createTempFile(".temp-ffmpeg-files-$id", ".txt").apply {
                writeText(content)
            }
        }

        fun concatenateVideoFiles(
            inputFiles: Iterable<String>,
            outputFile: String,
            extraCommand: String = "",
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
                        " -y" +
                        " $outputFile"

            FFmpegKit.executeAsync(
                command
            ) { session ->
                runCatching {
                    listFile.delete()
                }

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

                    completer.completeExceptionally(FFmpegException("Failed to concatenate videos"))
                } else {
                    completer.complete(Unit)
                }
            }

            return completer
        }
    }

    class FFmpegException(message: String) : Exception(message)
}