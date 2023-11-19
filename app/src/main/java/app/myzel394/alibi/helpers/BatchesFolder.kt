package app.myzel394.alibi.helpers

import android.content.Context
import androidx.documentfile.provider.DocumentFile
import app.myzel394.alibi.ui.RECORDER_SUBFOLDER_NAME
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.arthenica.ffmpegkit.FFmpegKitConfig
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.FileDescriptor

data class BatchesFolder(
    val context: Context,
    val type: BatchType,
    val customFolder: DocumentFile? = null,
    val subfolderName: String = ".recordings",
) {
    private var customFileFileDescriptor: ParcelFileDescriptor? = null

    fun initFolders() {
        when (type) {
            BatchType.INTERNAL -> getFolder(context).mkdirs()
            BatchType.CUSTOM -> {
                if (customFolder!!.findFile(subfolderName) == null) {
                    customFolder.createDirectory(subfolderName)
                }
            }
        }
    }

    fun cleanup() {
        customFileFileDescriptor?.close()
    }

    private fun getInternalFolder(): File {
        return getFolder(context)
    }

    private fun getCustomDefinedFolder(): DocumentFile {
        return customFolder!!.findFile(subfolderName)!!
    }

    fun getBatchesForFFmpeg(): List<String> {
        return when (type) {
            BatchType.INTERNAL ->
                (getInternalFolder()
                    .listFiles()
                    ?.filter {
                        it.nameWithoutExtension.toIntOrNull() != null
                    }
                    ?.toList()
                    ?: emptyList())
                    .map { it.absolutePath }

            BatchType.CUSTOM -> getCustomDefinedFolder()
                .listFiles()
                .filter {
                    it.name?.substringBeforeLast(".")?.toIntOrNull() != null
                }
                .map {
                    FFmpegKitConfig.getSafParameterForRead(
                        context,
                        it.uri,
                    )!!
                }
        }
    }

    fun getName(date: LocalDateTime, extension: String): String {
        val name = date
            .format(DateTimeFormatter.ISO_DATE_TIME)
            .toString()
            .replace(":", "-")
            .replace(".", "_")

        return "$name.$extension"
    }

    fun asInternalGetOutputFile(date: LocalDateTime, extension: String): File {
        return File(getInternalFolder(), getName(date, extension))
    }

    fun asCustomGetOutputFile(
        date: LocalDateTime,
        extension: String,
    ): DocumentFile {
        return getCustomDefinedFolder().createFile("audio/$extension", getName(date, extension))!!
    }

    fun getOutputFileForFFmpeg(
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

    fun checkIfOutputAlreadyExists(
        date: LocalDateTime,
        extension: String
    ): Boolean {
        val name = date
            .format(DateTimeFormatter.ISO_DATE_TIME)
            .toString()
            .replace(":", "-")
            .replace(".", "_")

        return when (type) {
            BatchType.INTERNAL -> File(getInternalFolder(), "$name.$extension").exists()
            BatchType.CUSTOM ->
                getCustomDefinedFolder().findFile("${name}.${extension}")?.exists() ?: false
        }
    }

    fun exportFolderForSettings(): String {
        return when (type) {
            BatchType.INTERNAL -> "_'internal"
            BatchType.CUSTOM -> customFolder!!.uri.toString()
        }
    }

    fun deleteRecordings() {
        when (type) {
            BatchType.INTERNAL -> getInternalFolder().deleteRecursively()
            BatchType.CUSTOM -> customFolder?.findFile(subfolderName)?.delete()
        }
    }

    fun hasRecordingsAvailable(): Boolean {
        return when (type) {
            BatchType.INTERNAL -> getInternalFolder().listFiles()?.isNotEmpty() ?: false
            BatchType.CUSTOM -> getCustomDefinedFolder().listFiles().isNotEmpty()
        }
    }

    fun deleteOldRecordings(earliestCounter: Long) {
        when (type) {
            BatchType.INTERNAL -> getInternalFolder().listFiles()?.forEach {
                val fileCounter = it.nameWithoutExtension.toIntOrNull() ?: return@forEach

                if (fileCounter < earliestCounter) {
                    it.delete()
                }
            }

            BatchType.CUSTOM -> getCustomDefinedFolder().listFiles().forEach {
                val fileCounter = it.name?.substringBeforeLast(".")?.toIntOrNull() ?: return@forEach

                if (fileCounter < earliestCounter) {
                    it.delete()
                }
            }
        }
    }

    fun checkIfFolderIsAccessible(): Boolean {
        return when (type) {
            BatchType.INTERNAL -> true
            BatchType.CUSTOM -> getCustomDefinedFolder().canWrite() && getCustomDefinedFolder().canRead()
        }
    }

    fun asInternalGetOutputPath(counter: Long, fileExtension: String): String {
        return getInternalFolder().absolutePath + "/$counter.$fileExtension"
    }

    fun asCustomGetFileDescriptor(
        counter: Long,
        fileExtension: String,
    ): FileDescriptor {
        val file =
            getCustomDefinedFolder().createFile("audio/$fileExtension", "$counter.$fileExtension")!!

        customFileFileDescriptor = context.contentResolver.openFileDescriptor(file.uri, "w")!!

        return customFileFileDescriptor!!.fileDescriptor
    }

    enum class BatchType {
        INTERNAL,
        CUSTOM,
    }

    companion object {
        fun viaInternalFolder(context: Context): BatchesFolder {
            return BatchesFolder(context, BatchType.INTERNAL)
        }

        fun viaCustomFolder(context: Context, folder: DocumentFile): BatchesFolder {
            return BatchesFolder(context, BatchType.CUSTOM, folder)
        }

        fun getFolder(context: Context) = File(context.filesDir, RECORDER_SUBFOLDER_NAME)

        fun importFromFolder(folder: String, context: Context): BatchesFolder = when (folder) {
            "_'internal" -> viaInternalFolder(context)
            else -> viaCustomFolder(context, DocumentFile.fromTreeUri(context, Uri.parse(folder))!!)
        }
    }
}

