package app.myzel394.alibi.helpers

import app.myzel394.alibi.ui.MEDIA_RECORDINGS_PREFIX

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore.Video.Media
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.arthenica.ffmpegkit.FFmpegKitConfig
import android.util.Log
import kotlinx.coroutines.CompletableDeferred
import kotlin.reflect.KFunction3

abstract class BatchesFolder(
    open val context: Context,
    open val type: BatchType,
    open val customFolder: DocumentFile? = null,
    open val subfolderName: String = ".recordings",
) {
    abstract val concatenationFunction: KFunction3<Iterable<String>, String, String, CompletableDeferred<Unit>>
    abstract val ffmpegParameters: Array<String>
    abstract val mediaContentUri: Uri

    val mediaPrefix
        get() = MEDIA_RECORDINGS_PREFIX + subfolderName

    fun initFolders() {
        when (type) {
            BatchType.INTERNAL -> getInternalFolder().mkdirs()

            BatchType.CUSTOM -> {
                if (customFolder!!.findFile(subfolderName) == null) {
                    customFolder!!.createDirectory(subfolderName)
                }
            }

            BatchType.MEDIA -> {
                // Add support for < Android 10
            }
        }
    }

    fun getInternalFolder(): File {
        return File(context.filesDir, subfolderName)
    }

    fun getCustomDefinedFolder(): DocumentFile {
        return customFolder!!.findFile(subfolderName)!!
    }

    protected fun queryMediaContent(
        callback: (rawName: String, counter: Int, uri: Uri, cursor: Cursor) -> Any?,
    ) {
        context.contentResolver.query(
            mediaContentUri,
            null,
            null,
            null,
            null,
        )!!.use { cursor ->
            while (cursor.moveToNext()) {
                val rawName = cursor.getColumnIndex(Media.DISPLAY_NAME).let { id ->
                    if (id == -1) "" else cursor.getString(id)
                }

                if (rawName == "" || rawName == null) {
                    continue
                }

                if (!rawName.startsWith(mediaPrefix)) {
                    continue
                }

                val counter =
                    rawName.substringAfter(mediaPrefix).substringBeforeLast(".").toIntOrNull()
                        ?: continue

                val id = cursor.getColumnIndex(Media._ID).let { id ->
                    if (id == -1) "" else cursor.getString(id)
                }

                if (id == "" || id == null) {
                    continue
                }

                val uri = Uri.withAppendedPath(mediaContentUri, id)

                val result = callback(rawName, counter, uri, cursor)

                if (result != null) {
                    return
                }
            }
        }
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

            BatchType.MEDIA -> {
                val filePaths = mutableListOf<String>()

                queryMediaContent { _, _, uri, _ ->
                    filePaths.add(
                        FFmpegKitConfig.getSafParameterForRead(
                            context,
                            uri,
                        )!!
                    )
                }

                filePaths
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

    fun checkIfOutputAlreadyExists(
        date: LocalDateTime,
        extension: String
    ): Boolean {
        val stem = date
            .format(DateTimeFormatter.ISO_DATE_TIME)
            .toString()
            .replace(":", "-")
            .replace(".", "_")
        val fileName = "$stem.$extension"

        return when (type) {
            BatchType.INTERNAL -> File(getInternalFolder(), fileName).exists()

            BatchType.CUSTOM ->
                getCustomDefinedFolder().findFile(fileName)?.exists() ?: false

            BatchType.MEDIA -> {
                var exists = false

                queryMediaContent { rawName, _, _, _ ->
                    if (rawName == fileName) {
                        exists = true
                        return@queryMediaContent true
                    } else {
                    }
                }

                exists
            }
        }
    }

    abstract fun getOutputFileForFFmpeg(
        date: LocalDateTime,
        extension: String,
    ): String

    abstract fun cleanup()

    open suspend fun concatenate(
        recordingStart: LocalDateTime,
        extension: String,
        disableCache: Boolean = false,
        onNextParameterTry: (String) -> Unit = {},
    ): String {
        if (!disableCache && checkIfOutputAlreadyExists(recordingStart, extension)) {
            return getOutputFileForFFmpeg(
                date = recordingStart,
                extension = extension,
            )
        }

        for (parameter in ffmpegParameters) {
            Log.i("Concatenation", "Trying parameter $parameter")
            onNextParameterTry(parameter)

            try {
                val filePaths = getBatchesForFFmpeg()
                val outputFile = getOutputFileForFFmpeg(
                    date = recordingStart,
                    extension = extension,
                )

                concatenationFunction(
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
            BatchType.MEDIA -> "_'media"
        }
    }

    fun deleteRecordings() {
        when (type) {
            BatchType.INTERNAL -> getInternalFolder().deleteRecursively()

            BatchType.CUSTOM -> customFolder?.findFile(subfolderName)?.delete()
                ?: customFolder?.findFile(subfolderName)?.listFiles()?.forEach {
                    it.delete()
                }

            BatchType.MEDIA -> {
                queryMediaContent { _, _, uri, _ ->
                    context.contentResolver.delete(
                        uri,
                        null,
                        null,
                    )
                }
            }
        }
    }

    fun hasRecordingsAvailable(): Boolean {
        return when (type) {
            BatchType.INTERNAL -> getInternalFolder().listFiles()?.isNotEmpty() ?: false

            BatchType.CUSTOM -> customFolder?.findFile(subfolderName)?.listFiles()?.isNotEmpty()
                ?: false

            BatchType.MEDIA -> {
                var hasRecordings = false

                queryMediaContent { _, _, _, _ ->
                    hasRecordings = true
                    return@queryMediaContent true
                }

                hasRecordings
            }
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

            BatchType.MEDIA -> {
                queryMediaContent { _, counter, uri, _ ->
                    if (counter < earliestCounter) {
                        context.contentResolver.delete(
                            uri,
                            null,
                            null,
                        )
                    }
                }
            }
        }
    }

    fun checkIfFolderIsAccessible(): Boolean {
        return when (type) {
            BatchType.INTERNAL -> true
            BatchType.CUSTOM -> getCustomDefinedFolder().canWrite() && getCustomDefinedFolder().canRead()
            // Add support for < Android 10
            BatchType.MEDIA -> true
        }
    }

    fun asInternalGetFile(counter: Long, fileExtension: String): File {
        return File(getInternalFolder(), "$counter.$fileExtension")
    }

    enum class BatchType {
        INTERNAL,
        CUSTOM,
        MEDIA,
    }
}

