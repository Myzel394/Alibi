package app.myzel394.alibi.helpers

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.os.storage.StorageManager
import android.provider.MediaStore
import android.provider.MediaStore.Video.Media
import android.system.Os
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.db.RecordingInformation
import app.myzel394.alibi.ui.MEDIA_RECORDINGS_PREFIX
import app.myzel394.alibi.ui.RECORDER_INTERNAL_SELECTED_VALUE
import app.myzel394.alibi.ui.RECORDER_MEDIA_SELECTED_VALUE
import app.myzel394.alibi.ui.SUPPORTS_SCOPED_STORAGE
import app.myzel394.alibi.ui.utils.PermissionHelper
import com.arthenica.ffmpegkit.FFmpegKitConfig
import kotlinx.coroutines.CompletableDeferred
import java.io.Closeable
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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

    data class BatchFile(
        val counter: Long,
        val ffmpegPath: String,
    )

    protected data class UriBatchFile(
        val counter: Long,
        val uri: Uri,
        val rawName: String,
    )

    protected class FFmpegInputPaths(
        val paths: List<String>,
        private val closeables: List<Closeable> = emptyList(),
    ) : Closeable {
        override fun close() {
            closeables.forEach {
                runCatching {
                    it.close()
                }
            }
        }
    }

    protected class FFmpegOutputTarget(
        val ffmpegPath: String,
        private val closeable: Closeable? = null,
        private val onCommit: () -> String = { ffmpegPath },
        private val onAbort: () -> Unit = {},
    ) : Closeable {
        private var closed = false
        private var finished = false

        override fun close() {
            if (closed) {
                return
            }

            runCatching {
                closeable?.close()
            }
            closed = true
        }

        fun commit(): String {
            close()
            return try {
                onCommit().also {
                    finished = true
                }
            } catch (error: Exception) {
                onAbort()
                finished = true
                throw error
            }
        }

        fun abort() {
            if (finished) {
                return
            }

            close()
            onAbort()
            finished = true
        }
    }

    protected class FFmpegFileDescriptorPath(
        val path: String,
        private val parcelFileDescriptor: ParcelFileDescriptor,
    ) : Closeable {
        override fun close() {
            parcelFileDescriptor.close()
        }
    }

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

    private fun Cursor.mediaSize(): Long? {
        val sizeColumn = getColumnIndex(MediaStore.MediaColumns.SIZE)
        if (sizeColumn == -1 || isNull(sizeColumn)) {
            return null
        }

        return getLong(sizeColumn)
    }

    private fun isUsableMediaSize(size: Long?) = size == null || size > 0L

    fun getBatchesForFFmpeg(): List<String> =
        getBatchesForFFmpegBatches().map {
            it.ffmpegPath
        }

    protected fun openUriPathForFFmpeg(
        uri: Uri,
        mode: String,
    ): FFmpegFileDescriptorPath {
        val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, mode)
            ?: throw MediaConverter.FFmpegException("Unable to open media file")

        return FFmpegFileDescriptorPath(
            path = "fd:${parcelFileDescriptor.fd}",
            parcelFileDescriptor = parcelFileDescriptor,
        )
    }

    protected fun getUriBatchesForFFmpeg(): List<UriBatchFile> {
        return when (type) {
            BatchType.CUSTOM -> getCustomDefinedFolder()
                .listFiles()
                .filter {
                    it.name?.substringBeforeLast(".")?.toIntOrNull() != null && it.length() > 0L
                }
                .sortedBy {
                    it.name!!.substringBeforeLast(".").toInt()
                }
                .map {
                    UriBatchFile(
                        counter = it.name!!.substringBeforeLast(".").toLong(),
                        uri = it.uri,
                        rawName = it.name!!,
                    )
                }

            BatchType.MEDIA -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    throw IllegalStateException("Legacy media batches do not use content URIs")
                }

                val fileUris = mutableListOf<UriBatchFile>()

                queryMediaContent { rawName, counter, uri, cursor ->
                    if (isUsableMediaSize(cursor.mediaSize())) {
                        fileUris.add(
                            UriBatchFile(
                                counter = counter.toLong(),
                                uri = uri,
                                rawName = rawName,
                            )
                        )
                    }
                }

                fileUris
                    .sortedBy {
                        it.rawName
                            .substring(mediaPrefix.length)
                            .substringBeforeLast(".")
                            .toInt()
                    }
            }

            BatchType.INTERNAL -> throw IllegalStateException("Internal batches do not use content URIs")
        }
    }

    protected open fun prepareInputPathsForFFmpeg(extension: String): FFmpegInputPaths =
        FFmpegInputPaths(getBatchesForFFmpeg())

    protected open fun prepareOutputTargetForFFmpeg(
        date: LocalDateTime,
        extension: String,
        fileName: String,
    ): FFmpegOutputTarget {
        val outputFile = getOutputFileForFFmpeg(
            date = date,
            extension = extension,
            fileName = fileName,
        )

        return FFmpegOutputTarget(
            ffmpegPath = outputFile,
            onCommit = { outputFile },
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    protected fun createMediaFile(
        name: String,
        mimeType: String,
        relativePath: String,
        isPending: Boolean = false,
    ): Uri {
        return context.contentResolver.insert(
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
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    relativePath,
                )
                put(
                    MediaStore.MediaColumns.IS_PENDING,
                    if (isPending) 1 else 0,
                )
            }
        ) ?: throw MediaConverter.FFmpegException("Unable to create export destination")
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    protected fun deleteMediaFileByName(fileName: String) {
        context.contentResolver.delete(
            scopedMediaContentUri,
            "${MediaStore.MediaColumns.DISPLAY_NAME} = ?",
            arrayOf(fileName),
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    protected fun publishPendingMediaFile(
        uri: Uri,
        fileName: String,
    ) {
        deleteMediaFileByName(fileName)

        val updated = context.contentResolver.update(
            uri,
            ContentValues().apply {
                put(
                    MediaStore.MediaColumns.DISPLAY_NAME,
                    fileName,
                )
                put(
                    MediaStore.MediaColumns.IS_PENDING,
                    0,
                )
            },
            null,
            null,
        )

        if (updated != 1) {
            throw MediaConverter.FFmpegException("Unable to publish export destination")
        }
    }

    open fun getBatchCountersForFFmpeg(): List<Long> {
        return when (type) {
            BatchType.INTERNAL ->
                getInternalFolder()
                    .listFiles()
                    ?.filter {
                        it.nameWithoutExtension.toIntOrNull() != null && it.length() > 0L
                    }
                    ?.sortedBy {
                        it.nameWithoutExtension.toInt()
                    }
                    ?.map {
                        it.nameWithoutExtension.toLong()
                    } ?: emptyList()

            BatchType.CUSTOM -> getCustomDefinedFolder()
                .listFiles()
                .filter {
                    it.name?.substringBeforeLast(".")?.toIntOrNull() != null && it.length() > 0L
                }
                .sortedBy {
                    it.name!!.substringBeforeLast(".").toInt()
                }
                .map {
                    it.name!!.substringBeforeLast(".").toLong()
                }

            BatchType.MEDIA -> {
                val counters = mutableListOf<Pair<String, Long>>()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    queryMediaContent { rawName, counter, _, cursor ->
                        if (isUsableMediaSize(cursor.mediaSize())) {
                            counters.add(Pair(rawName, counter.toLong()))
                        }
                    }
                } else {
                    legacyMediaFolder.listFiles()?.forEach {
                        if (it.name.startsWith(mediaPrefix) && it.length() > 0L) {
                            counters.add(
                                Pair(
                                    it.name,
                                    it.name
                                        .substring(mediaPrefix.length)
                                        .substringBeforeLast(".")
                                        .toLongOrNull() ?: return@forEach
                                )
                            )
                        }
                    }
                }

                counters
                    .sortedBy { (name, _) ->
                        name
                            .substring(mediaPrefix.length)
                            .substringBeforeLast(".")
                            .toInt()
                    }
                    .map { (_, counter) -> counter }
            }
        }
    }

    fun getBatchesForFFmpegBatches(): List<BatchFile> {
        return when (type) {
            BatchType.INTERNAL ->
                ((getInternalFolder()
                    .listFiles()
                    ?.filter {
                        it.nameWithoutExtension.toIntOrNull() != null && it.length() > 0L
                    }
                    ?.toList()
                    ?: emptyList()) as List<File>)
                    .sortedBy {
                        it.nameWithoutExtension.toInt()
                    }
                    .map {
                        BatchFile(
                            counter = it.nameWithoutExtension.toLong(),
                            ffmpegPath = it.absolutePath,
                        )
                    }

            BatchType.CUSTOM -> getCustomDefinedFolder()
                .listFiles()
                .filter {
                    it.name?.substringBeforeLast(".")?.toIntOrNull() != null && it.length() > 0L
                }
                .sortedBy {
                    it.name!!.substringBeforeLast(".").toInt()
                }
                .map {
                    BatchFile(
                        counter = it.name!!.substringBeforeLast(".").toLong(),
                        ffmpegPath = FFmpegKitConfig.getSafParameterForRead(
                            context,
                            it.uri,
                        )!!,
                    )
                }

            BatchType.MEDIA -> {
                val fileUris = mutableListOf<Triple<String, Long, Uri>>()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    queryMediaContent { rawName, counter, uri, cursor ->
                        if (isUsableMediaSize(cursor.mediaSize())) {
                            fileUris.add(Triple(rawName, counter.toLong(), uri))
                        }
                    }
                } else {
                    legacyMediaFolder.listFiles()?.forEach {
                        if (it.name.startsWith(mediaPrefix) && it.length() > 0L) {
                            val counter = it.name
                                .substring(mediaPrefix.length)
                                .substringBeforeLast(".")
                                .toLongOrNull() ?: return@forEach

                            fileUris.add(Triple(it.name, counter, it.toUri()))
                        }
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
                    .map { (_, counter, uri) ->
                        BatchFile(
                            counter = counter,
                            ffmpegPath = FFmpegKitConfig.getSafParameterForRead(
                                context,
                                uri,
                            )!!,
                        )
                    }
            }
        }
    }

    fun getBatchesAmount(): Int {
        return when (type) {
            BatchType.INTERNAL ->
                getInternalFolder()
                    .listFiles()
                    ?.count {
                        it.nameWithoutExtension.toIntOrNull() != null && it.length() > 0L
                    } ?: 0

            BatchType.CUSTOM -> getCustomDefinedFolder()
                .listFiles()
                .count {
                    it.name?.substringBeforeLast(".")?.toIntOrNull() != null && it.length() > 0L
                }

            BatchType.MEDIA -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    var count = 0
                    queryMediaContent { _, _, _, cursor ->
                        if (isUsableMediaSize(cursor.mediaSize())) {
                            count += 1
                        }
                        true
                    }
                    return count
                }

                legacyMediaFolder
                    .listFiles()
                    ?.count {
                        if (!it.name.startsWith(mediaPrefix)) {
                            return@count false
                        }

                        it.name
                            .substring(mediaPrefix.length)
                            .substringBeforeLast(".")
                            .toIntOrNull() != null && it.length() > 0L
                    } ?: 0
            }
        }
    }

    open fun getFFmpegParameters(
        recording: RecordingInformation,
        batchCounters: List<Long>,
    ): Array<String> = ffmpegParameters

    fun getName(date: LocalDateTime, extension: String): String {
        val name = date
            .format(DateTimeFormatter.ISO_DATE_TIME)
            .toString()
            .replace(":", "-")
            .replace(".", "_")

        return "$name.$extension"
    }

    fun asInternalGetOutputFile(fileName: String): File {
        return File(getInternalFolder(), fileName)
    }

    fun asMediaGetLegacyFile(name: String): File = File(
        legacyMediaFolder,
        name
    ).apply {
        createNewFile()
    }

    fun checkIfOutputAlreadyExists(
        fileName: String,
    ): Boolean {
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
        fileName: String,
    ): String

    abstract fun cleanup()

    suspend fun concatenate(
        recording: RecordingInformation,
        filenameFormat: AppSettings.FilenameFormat,
        disableCache: Boolean? = null,
        onNextParameterTry: (String) -> Unit = {},
        onProgress: (Float?) -> Unit = {},
        fileName: String,
    ): String {
        val disableCache = disableCache ?: (type != BatchType.INTERNAL)
        val date = recording.getStartDateForFilename(filenameFormat)

        if (!disableCache && checkIfOutputAlreadyExists(fileName)
        ) {
            return getOutputFileForFFmpeg(
                date = recording.recordingStart,
                extension = recording.fileExtension,
                fileName = fileName,
            )
        }

        for (parameter in getFFmpegParameters(recording, getBatchCountersForFFmpeg())) {
            Log.i("Concatenation", "Trying parameter $parameter")
            onNextParameterTry(parameter)
            onProgress(null)

            var inputPaths: FFmpegInputPaths? = null
            var outputTarget: FFmpegOutputTarget? = null
            var completed = false

            try {
                val fullTime = recording.getFullDuration().toFloat();
                inputPaths = prepareInputPathsForFFmpeg(recording.fileExtension)
                if (inputPaths.paths.isEmpty()) {
                    throw MediaConverter.FFmpegException("No valid media batches available")
                }

                outputTarget = prepareOutputTargetForFFmpeg(
                    date = date,
                    extension = recording.fileExtension,
                    fileName = fileName,
                )

                concatenationFunction(
                    inputPaths.paths,
                    outputTarget.ffmpegPath,
                    parameter
                ) { time ->
                    // The progressbar for the conversion is calculated based on the
                    // current time of the conversion and the total time of the batches.
                    onProgress(time / fullTime)
                }.await()

                val result = outputTarget.commit()
                completed = true
                return result
            } catch (e: MediaConverter.FFmpegException) {
                continue
            } finally {
                if (!completed) {
                    outputTarget?.abort()
                }
                inputPaths?.close()
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

    fun deleteRecordings(range: LongRange) {
        when (type) {
            BatchType.INTERNAL -> getInternalFolder().listFiles()?.forEach {
                val fileCounter = it.nameWithoutExtension.toIntOrNull() ?: return@forEach

                if (fileCounter in range) {
                    it.delete()
                }
            }

            BatchType.CUSTOM -> getCustomDefinedFolder().listFiles().forEach {
                val fileCounter = it.name?.substringBeforeLast(".")?.toIntOrNull() ?: return@forEach

                if (fileCounter in range) {
                    it.delete()
                }
            }

            BatchType.MEDIA -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val deletableNames = mutableListOf<String>()

                    queryMediaContent { rawName, counter, _, _ ->
                        if (counter in range) {
                            deletableNames.add(rawName)
                        }
                    }

                    try {
                        context.contentResolver.delete(
                            scopedMediaContentUri,
                            "${MediaStore.MediaColumns.DISPLAY_NAME} IN (${
                                deletableNames.joinToString(
                                    ","
                                ) { "'$it'" }
                            })",
                            null,
                        )
                        // This is unfortunate if the files can't be deleted, but let's just
                        // ignore it since we can't do anything about it
                    } catch (e: RuntimeException) {
                        // Probably file not found
                        e.printStackTrace()
                    } catch (e: IllegalArgumentException) {
                        // Strange filename, should not happen
                        e.printStackTrace()
                    }
                } else {
                    // TODO: Fix "would you like to try saving" -> Save button
                    legacyMediaFolder.listFiles()?.forEach {
                        val fileCounter =
                            it.nameWithoutExtension.substring(mediaPrefix.length).toIntOrNull()
                                ?: return@forEach

                        if (fileCounter in range) {
                            it.delete()
                        }
                    }
                }
            }
        }
    }

    fun checkIfFolderIsAccessible(): Boolean {
        try {
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
        } catch (error: NullPointerException) {
            error.printStackTrace()
            return false
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
                            MediaStore.MediaColumns.RELATIVE_PATH,
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

    fun getAvailableBytes(): Long? {
        if (type == BatchType.CUSTOM) {
            var fileDescriptor: ParcelFileDescriptor? = null

            try {
                fileDescriptor =
                    context.contentResolver.openFileDescriptor(customFolder!!.uri, "r")!!
                val stats = Os.fstatvfs(fileDescriptor.fileDescriptor)

                val available = stats.f_bavail * stats.f_bsize

                runCatching {
                    fileDescriptor.close()
                }

                return available
            } catch (e: Exception) {
                runCatching {
                    fileDescriptor?.close();
                }

                return null
            }
        }

        val storageManager = context.getSystemService(StorageManager::class.java) ?: return null
        val file = when (type) {
            BatchType.INTERNAL -> context.filesDir
            BatchType.MEDIA ->
                if (SUPPORTS_SCOPED_STORAGE)
                    File(
                        Environment.getExternalStoragePublicDirectory(VideoBatchesFolder.BASE_SCOPED_STORAGE_RELATIVE_PATH),
                        Media.EXTERNAL_CONTENT_URI.toString(),
                    )
                else
                    File(
                        Environment.getExternalStoragePublicDirectory(VideoBatchesFolder.BASE_LEGACY_STORAGE_FOLDER),
                        VideoBatchesFolder.MEDIA_RECORDINGS_SUBFOLDER,
                    )

            BatchType.CUSTOM -> throw IllegalArgumentException("This code should not be reachable")
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            storageManager.getAllocatableBytes(storageManager.getUuidForPath(file))
        } else {
            file.usableSpace;
        }
    }

    enum class BatchType {
        INTERNAL,
        CUSTOM,
        MEDIA,
    }

    companion object {
        fun requiredBytesForOneMinuteOfRecording(appSettings: AppSettings): Long {
            // 350 MiB sounds like a good default
            return 350 * 1024 * 1024
        }

        fun canAccessFolder(context: Context, uri: Uri): Boolean {
            // This always returns false for some reason, let's just assume it's true
            return true
            /*
            return try {
                // Create temp file
                val docFile = DocumentFile.fromSingleUri(context, uri)!!

                return docFile.canWrite().also {
                    println("Can write? ${it}")
                } && docFile.canRead().also {
                    println("Can read? ${it}")
                }
            } catch (error: RuntimeException) {
                error.printStackTrace()
                false
            }
             */
        }
    }
}
