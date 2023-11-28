package app.myzel394.alibi.helpers

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.arthenica.ffmpegkit.FFmpegKitConfig
import java.time.LocalDateTime

class VideoBatchesFolder(
    override val context: Context,
    override val type: BatchesFolder.BatchType,
    override val customFolder: DocumentFile? = null,
    override val subfolderName: String = ".video_recordings",
) : BatchesFolder(
    context,
    type,
    customFolder,
    subfolderName,
) {
    override fun getOutputFileForFFmpeg(date: LocalDateTime, extension: String): String {
        return when (type) {
            BatchType.INTERNAL -> asInternalGetOutputFile(date, extension).absolutePath
            BatchType.CUSTOM -> FFmpegKitConfig.getSafParameterForWrite(
                context,
                customFolder!!.createFile(
                    "video/${extension}",
                    getName(date, extension),
                )!!.uri
            )!!
        }
    }

    override suspend fun concatenate(
        recordingStart: LocalDateTime,
        extension: String,
        disableCache: Boolean
    ): String {
        val outputFile = getOutputFileForFFmpeg(
            date = recordingStart,
            extension = extension,
        )

        if (disableCache || !checkIfOutputAlreadyExists(recordingStart, extension)) {
            val filePaths = getBatchesForFFmpeg()

            MediaConverter.concatenateVideoFiles(
                inputFiles = filePaths,
                outputFile = outputFile,
            ).await()
        }

        return outputFile
    }

    companion object {
        fun viaInternalFolder(context: Context): BatchesFolder {
            return VideoBatchesFolder(context, BatchType.INTERNAL)
        }

        fun viaCustomFolder(context: Context, folder: DocumentFile): BatchesFolder {
            return VideoBatchesFolder(context, BatchType.CUSTOM, folder)
        }

        fun importFromFolder(folder: String, context: Context): BatchesFolder = when (folder) {
            "_'internal" -> AudioBatchesFolder.viaInternalFolder(context)
            else -> AudioBatchesFolder.viaCustomFolder(
                context,
                DocumentFile.fromTreeUri(context, Uri.parse(folder))!!
            )
        }
    }
}