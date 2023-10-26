package app.myzel394.alibi.helpers

import android.content.Context
import android.util.Log
import app.myzel394.alibi.db.RecordingInformation
import app.myzel394.alibi.ui.RECORDER_SUBFOLDER_NAME
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import java.io.File
import java.time.format.DateTimeFormatter

data class AudioRecorderExporter(
    val recording: RecordingInformation,
) {
    val filePaths: List<File>
        get() =
            File(recording.folderPath).listFiles()?.filter {
                val name = it.nameWithoutExtension

                name.toIntOrNull() != null
            }?.toList() ?: emptyList()

    val hasRecordingAvailable: Boolean
        get() = filePaths.isNotEmpty()

    private fun stripConcatenatedFileToExactDuration(
        outputFile: File
    ) {
        // Move the concatenated file to a temporary file
        val rawFile =
            File("${recording.folderPath}/${outputFile.nameWithoutExtension}-raw.${recording.fileExtension}")
        outputFile.renameTo(rawFile)

        val command = "-sseof ${recording.maxDuration / -1000} -i $rawFile -y $outputFile"

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

            throw Exception("Failed to strip concatenated audio")
        }
    }

    suspend fun concatenateFiles(forceConcatenation: Boolean = false): File {
        val paths = filePaths.joinToString("|")
        val fileName = recording.recordingStart
            .format(DateTimeFormatter.ISO_DATE_TIME)
            .toString()
            .replace(":", "-")
            .replace(".", "_")
        val outputFile = File("${recording.folderPath}/$fileName.${recording.fileExtension}")

        if (outputFile.exists() && !forceConcatenation) {
            return outputFile
        }

        val command = "-i 'concat:$paths' -y" +
                " -acodec copy" +
                " -metadata title='$fileName' " +
                " -metadata date='${recording.recordingStart.format(DateTimeFormatter.ISO_DATE_TIME)}'" +
                " -metadata batch_count='${filePaths.size}'" +
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
        if (recording.forceExactMaxDuration && filePaths.size > minRequiredForPossibleInExactMaxDuration) {
            stripConcatenatedFileToExactDuration(outputFile)
        }

        return outputFile
    }

    suspend fun cleanupFiles() {
        filePaths.forEach {
            runCatching {
                it.delete()
            }
        }
    }

    companion object {
        fun getFolder(context: Context) = File(context.filesDir, RECORDER_SUBFOLDER_NAME)

        fun clearAllRecordings(context: Context) {
            getFolder(context).deleteRecursively()
        }

        fun hasRecordingsAvailable(context: Context) =
            getFolder(context).listFiles()?.isNotEmpty() ?: false
    }
}