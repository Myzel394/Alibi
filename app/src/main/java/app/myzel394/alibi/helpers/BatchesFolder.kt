package app.myzel394.alibi.helpers

import android.content.Context
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.arthenica.ffmpegkit.FFmpegKitConfig
import android.os.ParcelFileDescriptor
import android.util.Log
import kotlinx.coroutines.CompletableDeferred
import java.io.FileDescriptor
import kotlin.reflect.KFunction3

abstract class BatchesFolder(
    open val context: Context,
    open val type: BatchType,
    open val customFolder: DocumentFile? = null,
    open val subfolderName: String = ".recordings",
) {
    private var customFileFileDescriptor: ParcelFileDescriptor? = null

    abstract val concatenateFunction: KFunction3<Iterable<String>, String, String, CompletableDeferred<Unit>>
    abstract val ffmpegParameters: Array<String>

    fun initFolders() {
        when (type) {
            BatchType.INTERNAL -> getInternalFolder().mkdirs()
            BatchType.CUSTOM -> {
                if (customFolder!!.findFile(subfolderName) == null) {
                    customFolder!!.createDirectory(subfolderName)
                }
            }
        }
    }

    fun cleanup() {
        customFileFileDescriptor?.close()
    }

    fun getInternalFolder(): File {
        return File(context.filesDir, subfolderName)
    }

    fun getCustomDefinedFolder(): DocumentFile {
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

    abstract fun getOutputFileForFFmpeg(
        date: LocalDateTime,
        extension: String,
    ): String

    open suspend fun concatenate(
        recordingStart: LocalDateTime,
        extension: String,
        disableCache: Boolean = false,
        onNextParameterTry: (String) -> Unit = {},
    ): String {
        val outputFile = getOutputFileForFFmpeg(
            date = recordingStart,
            extension = extension,
        )

        if (!disableCache && checkIfOutputAlreadyExists(recordingStart, extension)) {
            return outputFile
        }

        val filePaths = getBatchesForFFmpeg()

        for (parameter in ffmpegParameters) {
            Log.i("Concatenation", "Trying parameter $parameter")
            onNextParameterTry(parameter)

            try {
                concatenateFunction(
                    filePaths,
                    outputFile,
                    parameter,
                ).await()
                return outputFile
            } catch (e: MediaConverter.FFmpegException) {
                continue
            }
        }

        throw MediaConverter.FFmpegException("Failed to concatenate")
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
                ?: customFolder?.findFile(subfolderName)?.listFiles()?.forEach {
                    it.delete()
                }
        }
    }

    fun hasRecordingsAvailable(): Boolean {
        return when (type) {
            BatchType.INTERNAL -> getInternalFolder().listFiles()?.isNotEmpty() ?: false
            BatchType.CUSTOM -> customFolder?.findFile(subfolderName)?.listFiles()?.isNotEmpty()
                ?: false
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
}

