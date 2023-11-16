package app.myzel394.alibi.helpers

import android.content.Context
import android.net.Uri
import android.system.Os
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.net.toUri
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
    private fun getFilePaths(context: Context): List<File> =
        getFolder(context).listFiles()?.filter {
            val name = it.nameWithoutExtension

            name.toIntOrNull() != null
        }?.toList() ?: emptyList()

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

    suspend fun concatenateFiles(
        context: Context,
        uri: Uri,
        forceConcatenation: Boolean = false,
    ): File {
        val filePaths = getFilePaths(context)
        val paths = filePaths.joinToString("|") {
            it.path
        }
        val filePath = FFmpegKitConfig.getSafParameter(context, uri, "rw")
        println("!!!!!!!!!!!!!!!!!!1")
        println(getFolder(context).listFiles()?.map { it.name })
        println(filePath)
        val fileName = recording.recordingStart
            .format(DateTimeFormatter.ISO_DATE_TIME)
            .toString()
            .replace(":", "-")
            .replace(".", "_")
        val outputFile = File("${recording.folderPath}/$fileName.${recording.fileExtension}")

        if (outputFile.exists() && !forceConcatenation) {
            return outputFile
        }

        val command = "-protocol_whitelist saf,concat,content,file,subfile " +
                "-i 'concat:${filePath}' -y" +
                " -acodec copy" +
                " -metadata title='$fileName' " +
                " -metadata date='${recording.recordingStart.format(DateTimeFormatter.ISO_DATE_TIME)}'" +
                " -metadata batch_count='${filePaths.size}'" +
                " -metadata batch_duration='${recording.intervalDuration}'" +
                " -metadata max_duration='${recording.maxDuration}'" +
                " $outputFile"

        println("--------------------")
        println(command)
        println(outputFile)

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

    companion object {
        fun getFolder(context: Context) = File(context.filesDir, RECORDER_SUBFOLDER_NAME)

        fun clearAllRecordings(context: Context) {
            getFolder(context).deleteRecursively()
        }

        fun hasRecordingsAvailable(context: Context) =
            getFolder(context).listFiles()?.isNotEmpty() ?: false

        fun linkBatches(context: Context, batchesFolder: Uri, destinationFolder: File) {
            val folder = DocumentFile.fromTreeUri(
                context,
                batchesFolder,
            )!!

            destinationFolder.mkdirs()

            folder.listFiles().forEach {
                if (it.name?.substringBeforeLast(".")?.toIntOrNull() == null) {
                    return@forEach
                }

                println(
                    "symlinking ${folder.uri}/${it.name} to ${destinationFolder.absolutePath}/${it.name}"
                )

                Os.symlink(
                    "${folder.uri}/${it.name}",
                    "${destinationFolder.absolutePath}/${it.name}",
                )
            }
        }
    }
}