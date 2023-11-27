package app.myzel394.alibi.helpers

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.arthenica.ffmpegkit.FFmpegKitConfig
import java.time.LocalDateTime

class AudioBatchesFolder(
    override val context: Context,
    override val type: BatchType,
    override val customFolder: DocumentFile? = null,
    override val subfolderName: String = ".recordings",
) : BatchesFolder(
    context,
    type,
    customFolder,
    subfolderName,
) {
    override fun getOutputFileForFFmpeg(
        date: LocalDateTime,
        extension: String,
    ): String {
        return when (type) {
            BatchType.INTERNAL -> asInternalGetOutputFile(date, extension).absolutePath
            BatchType.CUSTOM -> FFmpegKitConfig.getSafParameterForWrite(
                context,
                customFolder!!.createFile(
                    "audio/${extension}",
                    getName(date, extension),
                )!!.uri
            )!!
        }
    }

    override suspend fun concatenate(
        recordingStart: LocalDateTime,
        extension: String,
        disableCache: Boolean,
    ) {
        if (!disableCache && checkIfOutputAlreadyExists(recordingStart, extension)) {
            return
        }

        val filePaths = getBatchesForFFmpeg()
        val outputFile = getOutputFileForFFmpeg(
            date = recordingStart,
            extension = extension,
        )

        MediaConverter.concatenateAudioFiles(
            inputFiles = filePaths,
            outputFile = outputFile,
        ).await()
    }

    companion object {
        fun viaInternalFolder(context: Context): BatchesFolder {
            return AudioBatchesFolder(context, BatchType.INTERNAL)
        }

        fun viaCustomFolder(context: Context, folder: DocumentFile): BatchesFolder {
            return AudioBatchesFolder(context, BatchType.CUSTOM, folder)
        }

        fun importFromFolder(folder: String, context: Context): BatchesFolder = when (folder) {
            "_'internal" -> viaInternalFolder(context)
            else -> viaCustomFolder(context, DocumentFile.fromTreeUri(context, Uri.parse(folder))!!)
        }
    }
}