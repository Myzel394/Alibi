package app.myzel394.alibi.helpers

import android.content.Context
import android.net.Uri
import android.system.Os
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import app.myzel394.alibi.db.RecordingInformation
import app.myzel394.alibi.ui.RECORDER_SUBFOLDER_NAME
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.arthenica.ffmpegkit.ReturnCode
import java.io.File
import java.time.format.DateTimeFormatter

data class AudioRecorderExporter(
    val recording: RecordingInformation,
) {
    suspend fun concatenateFiles(
        batchesFolder: BatchesFolder,
        outputFilePath: String,
        forceConcatenation: Boolean = false,
    ) {
        val filePaths = batchesFolder.getBatchesForFFmpeg()

        if (batchesFolder.checkIfOutputAlreadyExists(
                recording.recordingStart,
                recording.fileExtension
            ) && !forceConcatenation
        ) {
            return
        }

        val filePathsConcatenated = filePaths.joinToString("|")
        val command =
            "-protocol_whitelist saf,concat,content,file,subfile" +
                    " -i 'concat:$filePathsConcatenated' -y" +
                    " -acodec copy" +
                    " -metadata date='${recording.recordingStart.format(DateTimeFormatter.ISO_DATE_TIME)}'" +
                    " -metadata batch_count='${filePaths.size}'" +
                    " -metadata batch_duration='${recording.intervalDuration}'" +
                    " -metadata max_duration='${recording.maxDuration}'" +
                    " $outputFilePath"

        val session = FFmpegKit.execute(command)

        if (!ReturnCode.isSuccess(session.returnCode)) {
            Log.d(
                "Audio Concatenation",
                String.format(
                    "Command failed with state %s and rc %s.%s",
                    session.state,
                    session.returnCode,
                    session.failStackTrace,
                )
            )

            throw Exception("Failed to concatenate audios")
        }
    }

    companion object {
        fun getFolder(context: Context) = File(context.filesDir, RECORDER_SUBFOLDER_NAME)
    }
}

