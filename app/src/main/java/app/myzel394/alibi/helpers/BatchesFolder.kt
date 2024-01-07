package app.myzel394.alibi.helpers

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import app.myzel394.alibi.ui.MEDIA_RECORDINGS_PREFIX

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.MediaStore.Video.Media
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.arthenica.ffmpegkit.FFmpegKitConfig
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import app.myzel394.alibi.ui.RECORDER_INTERNAL_SELECTED_VALUE
import app.myzel394.alibi.ui.RECORDER_MEDIA_SELECTED_VALUE
import app.myzel394.alibi.ui.SUPPORTS_SCOPED_STORAGE
import app.myzel394.alibi.ui.utils.PermissionHelper
import com.arthenica.ffmpegkit.FFprobeKit
import kotlinx.coroutines.CompletableDeferred
import kotlin.reflect.KFunction4

abstract class BatchesFolder(
    open val context: Context,
    open val type: BatchType,
    open val customFolder: DocumentFile? = null,
    open val subfolderName: String = ".recordings",
) {
    abstract val concatenationFunction: KFunction4<Iterable<String>, String, String, (Int) -> Unit, CompletableDeferred<Unit>>
    abstract val ffmpegParameters: Array<String>
    abstract val scopedMediaContentUri: Uri
    abstract val legacyMediaFolder: File

    val mediaPrefix
        get() = MEDIA_RECORDINGS_PREFIX + subfolderName.substring(1) + "-"

    fun initFolders() {
        when (type) {
            BatchType.INTERNAL -> getInternalFolder().mkdirs()

            BatchType.CUSTOM -> {
                if (customFolder!!.findFile(subfolderName) == null) {
                    customFolder!!.createDirectory(subfolderName)
                }
            }

            BatchType.MEDIA -> {
                // Scoped storage works fine on new Android versions,
                // we need to manually manage the folder on older versions
                if (!SUPPORTS_SCOPED_STORAGE) {
                    legacyMediaFolder.mkdirs()
                }
            }
        }
    }

    fun getInternalFolder(): File {
        return File(context.filesDir, subfolderName)
    }

    fun getCustomDefinedFolder(): DocumentFile {
        return customFolder!!.findFile(subfolderName)!!
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    protected fun queryMediaContent(
        callback: (rawName: String, counter: Int, uri: Uri, cursor: Cursor) -> Any?,
    ) {
        context.contentResolver.query(
            scopedMediaContentUri,
            null,
            "${MediaStore.MediaColumns.DISPLAY_NAME} LIKE '$mediaPrefix%'",
            null,
            null,
        )!!.use { cursor ->
            while (cursor.moveToNext()) {
                val rawName = cursor.getColumnIndex(Media.DISPLAY_NAME).let { id ->
                    if (id == -1) null else cursor.getString(id)
                }

                if (rawName.isNullOrBlank() || !rawName.startsWith(mediaPrefix)) {
                    continue
                }

                val counter =
                    rawName.substringAfter(mediaPrefix).substringBeforeLast(".").toIntOrNull()
                        ?: continue

                val id = cursor.getColumnIndex(Media._ID).let { id ->
                    if (id == -1) null else cursor.getString(id)
                }

                if (id.isNullOrBlank()) {
                    continue
                }

                val uri = Uri.withAppendedPath(scopedMediaContentUri, id)

                val result = callback(rawName, counter, uri, cursor)

                if (result == false) {
                    return
                }
            }
        }
    }

    fun getBatchesForFFmpeg(): List<String> {
        return when (type) {
            BatchType.INTERNAL ->
                ((getInternalFolder()
                    .listFiles()
                    ?.filter {
                        it.nameWithoutExtension.toIntOrNull() != null
                    }
                    ?.toList()
                    ?: emptyList()) as List<File>)
                    .sortedBy {
                        it.nameWithoutExtension.toInt()
                    }
                    .map { it.absolutePath }

            BatchType.CUSTOM -> getCustomDefinedFolder()
                .listFiles()
                .filter {
                    it.name?.substringBeforeLast(".")?.toIntOrNull() != null
                }
                .sortedBy {
                    it.name!!.substringBeforeLast(".").toInt()
                }
                .map {
                    FFmpegKitConfig.getSafParameterForRead(
                        context,
                        it.uri,
                    )!!
                }

            BatchType.MEDIA -> {
                val fileUris = mutableListOf<Pair<String, Uri>>()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    queryMediaContent { rawName, _, uri, _ ->
                        fileUris.add(Pair(rawName, uri))
                    }
                } else {
                    legacyMediaFolder.listFiles()?.forEach {
                        fileUris.add(Pair(it.name, it.toUri()))
                    }
                }

                fileUris
                    .sortedBy {
                        val name = it.first

                        return@sortedBy name
                            .substring(mediaPrefix.length)
                            .substringBeforeLast(".")
                            .toInt()
                    }
                    .map { pair ->
                        val uri = pair.second

                        FFmpegKitConfig.getSafParameterForRead(
                            context,
                            uri,
                        )!!
                    }
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

    fun asMediaGetLegacyFile(name: String): File = File(
        legacyMediaFolder,
        name
    ).apply {
        createNewFile()
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

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    queryMediaContent { rawName, _, _, _ ->
                        if (rawName == fileName) {
                            exists = true
                            return@queryMediaContent true
                        } else {
                        }
                    }

                    return exists
                } else {
                    return File(
                        legacyMediaFolder,
                        fileName,
                    ).exists()
                }
            }
        }
    }

    abstract fun getOutputFileForFFmpeg(
        date: LocalDateTime,
        extension: String,
    ): String

    abstract fun cleanup()

    suspend fun concatenate(
        recordingStart: LocalDateTime,
        extension: String,
        disableCache: Boolean? = null,
        onNextParameterTry: (String) -> Unit = {},
        durationPerBatchInMilliseconds: Long = 0,
        onProgress: (Float?) -> Unit = {},
    ): String {
        val disableCache = disableCache ?: (type != BatchType.INTERNAL)

        if (!disableCache && checkIfOutputAlreadyExists(recordingStart, extension)) {
            return getOutputFileForFFmpeg(
                date = recordingStart,
                extension = extension,
            )
        }

        for (parameter in ffmpegParameters) {
            Log.i("Concatenation", "Trying parameter $parameter")
            onNextParameterTry(parameter)
            onProgress(null)

            try {
                val filePaths = getBatchesForFFmpeg()

                // Casting here to float so it doesn't need to redo it on every progress update
                var fullTime: Float? = null

                runCatching {
                    // `fullTime` is not accurate as the last batch might be shorter,
                    // but it's good enough for the progress bar

                    // Using the code below results in a nasty bug:
                    // since we use ffmpeg to extract the duration, the saf parameter is already
                    // "used up" and we can't use it again for the actual concatenation
                    // Since an accurate progress bar is less important than speed,
                    // we currently don't use this code
                    /*
                    val lastBatchTime = (FFprobeKit.execute(
                        "-i ${filePaths.last()} -show_entries format=duration -v quiet -of csv=\"p=0\"",
                    ).output.toFloat() * 1000).toLong()
                    fullTime =
                        ((durationPerBatchInMilliseconds * (filePaths.size - 1)) + lastBatchTime).toFloat()
                     */
                    // We use an approximation for the duration of the batches
                    fullTime = (durationPerBatchInMilliseconds * filePaths.size).toFloat()
                }

                val outputFile = getOutputFileForFFmpeg(
                    date = recordingStart,
                    extension = extension,
                )

                concatenationFunction(
                    filePaths,
                    outputFile,
                    parameter
                ) { time ->
                    // The progressbar for the conversion is calculated based on the
                    // current time of the conversion and the total time of the batches.
                    if (fullTime != null) {
                        onProgress(time / fullTime!!)
                    } else {
                        onProgress(null)
                    }
                }.await()
                return outputFile
            } catch (e: MediaConverter.FFmpegException) {
                continue
            }
        }

        throw MediaConverter.FFmpegException("Failed to concatenate")
    }

    fun exportFolderForSettings(): String {
        return when (type) {
            BatchType.INTERNAL -> RECORDER_INTERNAL_SELECTED_VALUE
            BatchType.MEDIA -> RECORDER_MEDIA_SELECTED_VALUE
            BatchType.CUSTOM -> customFolder!!.uri.toString()
        }
    }

    fun deleteRecordings() {
        // Currently deletes all recordings.
        // This is fine, because we are saving the recordings
        // in a dedicated subfolder
        when (type) {
            BatchType.INTERNAL -> getInternalFolder().deleteRecursively()

            BatchType.CUSTOM -> customFolder?.findFile(subfolderName)?.delete()
                ?: customFolder?.findFile(subfolderName)?.listFiles()?.forEach {
                    it.delete()
                }

            BatchType.MEDIA -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // TODO: Also delete pending recordings
                    // --> Doesn't seem to be possible :/
                    context.contentResolver.delete(
                        scopedMediaContentUri,
                        "${MediaStore.MediaColumns.DISPLAY_NAME} LIKE '$mediaPrefix%'",
                        null,
                    )

                } else {
                    legacyMediaFolder.deleteRecursively()
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

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    context.contentResolver.query(
                        scopedMediaContentUri,
                        arrayOf(MediaStore.MediaColumns.DISPLAY_NAME),
                        "${MediaStore.MediaColumns.DISPLAY_NAME} LIKE '$mediaPrefix%'",
                        null,
                        null,
                    )!!.use { cursor ->
                        if (cursor.moveToFirst()) {
                            hasRecordings = true
                        }
                    }

                    return hasRecordings
                } else {
                    return legacyMediaFolder.listFiles()?.isNotEmpty() ?: false
                }
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val deletableNames = mutableListOf<String>()

                    queryMediaContent { rawName, counter, _, _ ->
                        if (counter < earliestCounter) {
                            deletableNames.add(rawName)
                        }
                    }

                    context.contentResolver.delete(
                        scopedMediaContentUri,
                        "${MediaStore.MediaColumns.DISPLAY_NAME} IN (${deletableNames.joinToString(",")})",
                        null,
                    )
                } else {
                    // TODO: Fix "would you like to try saving" -> Save button
                    legacyMediaFolder.listFiles()?.forEach {
                        val fileCounter =
                            it.nameWithoutExtension.substring(mediaPrefix.length).toIntOrNull()
                                ?: return@forEach

                        if (fileCounter < earliestCounter) {
                            it.delete()
                        }
                    }
                }
            }
        }
    }

    fun checkIfFolderIsAccessible(): Boolean {
        return when (type) {
            BatchType.INTERNAL -> true
            BatchType.CUSTOM -> getCustomDefinedFolder().canWrite() && getCustomDefinedFolder().canRead()
            BatchType.MEDIA -> {
                if (SUPPORTS_SCOPED_STORAGE) {
                    return true
                }

                return PermissionHelper.hasGranted(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) &&
                        PermissionHelper.hasGranted(
                            context,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
            }
        }
    }

    fun asInternalGetFile(counter: Long, fileExtension: String): File {
        return File(getInternalFolder(), "$counter.$fileExtension")
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun getOrCreateMediaFile(
        name: String,
        mimeType: String,
        relativePath: String,
    ): Uri {
        // Check if already exists
        var uri: Uri? = null

        context.contentResolver.query(
            scopedMediaContentUri,
            arrayOf(MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DISPLAY_NAME),
            "${MediaStore.MediaColumns.DISPLAY_NAME} = '$name'",
            null,
            null,
        )!!.use { cursor ->
            if (cursor.moveToFirst()) {
                // No need to check for the name since the query already did that
                val id = cursor.getColumnIndex(MediaStore.MediaColumns._ID)

                if (id == -1) {
                    return@use
                }

                uri = ContentUris.withAppendedId(
                    scopedMediaContentUri,
                    cursor.getLong(id)
                )
            }
        }

        if (uri == null) {
            try {
                // Create empty output file to be able to write to it
                uri = context.contentResolver.insert(
                    scopedMediaContentUri,
                    ContentValues().apply {
                        put(
                            MediaStore.MediaColumns.DISPLAY_NAME,
                            name
                        )
                        put(
                            MediaStore.MediaColumns.MIME_TYPE,
                            mimeType
                        )

                        put(
                            Media.RELATIVE_PATH,
                            relativePath,
                        )
                    }
                )!!
            } catch (e: Exception) {
                Log.e("Media", "Failed to create file", e)
            }
        }

        return uri!!
    }

    enum class BatchType {
        INTERNAL,
        CUSTOM,
        MEDIA,
    }
}

