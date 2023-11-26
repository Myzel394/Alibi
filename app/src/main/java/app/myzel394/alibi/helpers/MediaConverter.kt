package app.myzel394.alibi.helpers

import android.util.Log
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import kotlinx.coroutines.CompletableDeferred

class MediaConverter {
    companion object {
        fun concatenate(
            inputFiles: Iterable<String>,
            outputFile: String,
            extraCommand: String = "",
        ): CompletableDeferred<Unit> {
            val completer = CompletableDeferred<Unit>()

            val filePathsConcatenated = inputFiles.joinToString("|")
            val command =
                "-protocol_whitelist saf,concat,content,file,subfile" +
                        " -i 'concat:$filePathsConcatenated' -y" +
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
}