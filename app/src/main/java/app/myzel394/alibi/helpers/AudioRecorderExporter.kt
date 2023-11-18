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
    private fun getInternalFilePaths(context: Context): List<File> =
        getFolder(context)
            .listFiles()
            ?.filter {
                val name = it.nameWithoutExtension

                name.toIntOrNull() != null
            }
            ?.toList()
            ?: emptyList()

    suspend fun concatenateFiles(
        context: Context,
        batchesFolder: BatchesFolder,
        forceConcatenation: Boolean = false,
    ) {
        val filePaths = batchesFolder.getBatchesForFFmpeg().joinToString("|")
        val outputFile =
            batchesFolder.getOutputFileForFFmpeg(recording.recordingStart, recording.fileExtension)

        val command =
            "-protocol_whitelist saf,concat,content,file,subfile" +
                    " -i 'concat:${filePaths}' -y" +
                    " -acodec copy" +
                    " -metadata date='${recording.recordingStart.format(DateTimeFormatter.ISO_DATE_TIME)}'" +
                    " -metadata batch_count='${filePaths.length}'" +
                    " -metadata batch_duration='${recording.intervalDuration}'" +
                    " -metadata max_duration='${recording.maxDuration}'" +
                    " $outputFile"

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

        val minRequiredForPossibleInExactMaxDuration =
            recording.maxDuration / recording.intervalDuration
    }

    companion object {
        fun getFolder(context: Context) = File(context.filesDir, RECORDER_SUBFOLDER_NAME)

        fun clearAllRecordings(context: Context) {
            getFolder(context).deleteRecursively()
        }

        fun hasRecordingsAvailable(context: Context) =
            getFolder(context).listFiles()?.isNotEmpty() ?: false

        fun linkBatches(context: Context, batchesFolder: Uri, destinationFolder: File) {
            val folder =
                DocumentFile.fromTreeUri(
                    context,
                    batchesFolder,
                )!!

            destinationFolder.mkdirs()

            folder.listFiles().forEach {
                if (it.name?.substringBeforeLast(".")?.toIntOrNull() == null) {
                    return@forEach
                }

                Os.symlink(
                    "${folder.uri}/${it.name}",
                    "${destinationFolder.absolutePath}/${it.name}",
                )
            }
        }
    }
}

