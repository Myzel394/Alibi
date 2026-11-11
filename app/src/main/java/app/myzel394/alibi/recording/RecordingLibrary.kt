package app.myzel394.alibi.recording

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.documentfile.provider.DocumentFile
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.helpers.AudioBatchesFolder
import app.myzel394.alibi.helpers.BatchesFolder
import app.myzel394.alibi.helpers.VideoBatchesFolder
import app.myzel394.alibi.ui.MEDIA_SUBFOLDER_NAME
import app.myzel394.alibi.ui.RECORDER_INTERNAL_SELECTED_VALUE
import app.myzel394.alibi.ui.RECORDER_MEDIA_SELECTED_VALUE
import java.io.File

/** Storage-neutral media entries. Only app recording directories are enumerated. */
data class RecordingFile(
    val uri: Uri, val name: String, val bytes: Long, val modified: Long,
    val video: Boolean, val fragment: Boolean, val mimeType: String,
    val file: File? = null, val document: DocumentFile? = null,
)
data class LibraryResult(val files: List<RecordingFile>, val errors: List<String>)

class RecordingLibrary(private val context: Context) {
    fun load(settings: AppSettings): LibraryResult {
        val result = linkedMapOf<String, RecordingFile>()
        val errors = mutableListOf<String>()
        fun add(item: RecordingFile) { result[item.uri.toString()] = item }
        fun guarded(label: String, block: () -> Unit) {
            try { block() } catch (error: Exception) { errors += "$label: ${error.localizedMessage ?: error.javaClass.simpleName}" }
        }
        for (video in listOf(false, true)) {
            guarded("Internal") {
                val folder = if (video) VideoBatchesFolder.viaInternalFolder(context) else AudioBatchesFolder.viaInternalFolder(context)
                folder.getInternalFolder().listFiles()?.filter { it.isFile && isMedia(it.name) }?.forEach {
                    add(fromFile(it, video, isFragment(it.name)))
                }
            }
            guarded("MediaStore") {
                if (Build.VERSION.SDK_INT >= 29) {
                    val collection = if (video) MediaStore.Video.Media.EXTERNAL_CONTENT_URI else MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    context.contentResolver.query(collection,
                        arrayOf(MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DISPLAY_NAME,
                            MediaStore.MediaColumns.SIZE, MediaStore.MediaColumns.DATE_MODIFIED, MediaStore.MediaColumns.MIME_TYPE),
                        "${MediaStore.MediaColumns.RELATIVE_PATH} LIKE ?",
                        arrayOf("%/$MEDIA_SUBFOLDER_NAME/%"), null)?.use { cursor ->
                        while (cursor.moveToNext()) {
                            val name = cursor.getString(1) ?: continue
                            add(RecordingFile(ContentUris.withAppendedId(collection, cursor.getLong(0)), name,
                                cursor.getLong(2), cursor.getLong(3) * 1000, video, isFragment(name),
                                cursor.getString(4) ?: mime(name, video)))
                        }
                    }
                } else {
                    val batches = if (video) VideoBatchesFolder.viaMediaFolder(context).legacyMediaFolder else AudioBatchesFolder.LEGACY_MEDIA_FOLDER
                    listOf(batches, batches.parentFile!!).forEach { dir ->
                        dir.listFiles()?.filter { it.isFile && isMedia(it.name) }?.forEach { add(fromFile(it, video, isFragment(it.name))) }
                    }
                }
            }
        }
        val trees = (context.contentResolver.persistedUriPermissions.map { it.uri.toString() } +
            listOfNotNull(settings.saveFolder, settings.lastRecording?.folderPath))
            .filter { it.startsWith("content:") }.distinct()
        trees.forEach { tree -> guarded("Custom folder") {
            val root = DocumentFile.fromTreeUri(context, Uri.parse(tree)) ?: error("Folder unavailable")
            if (!root.canRead()) error("Folder permission expired")
            for (video in listOf(false, true)) {
                root.findFile(if (video) ".video_recordings" else ".audio_recordings")?.listFiles()
                    ?.filter { it.isFile && isMedia(it.name ?: "") }?.forEach {
                        add(fromDocument(it, video, isFragment(it.name ?: "")))
                    }
            }
            // Existing exports are timestamp-named files at the selected tree root.
            root.listFiles().filter { it.isFile && isExportName(it.name ?: "") && isMedia(it.name ?: "") }.forEach {
                add(fromDocument(it, it.type?.startsWith("video/") == true, false))
            }
        } }
        return LibraryResult(result.values.sortedByDescending { it.modified }, errors)
    }
    fun delete(item: RecordingFile) {
        check(!RecordingRuntime.isBusy) { context.getString(app.myzel394.alibi.R.string.library_recording_busy) }
        val deleted = when {
            item.file != null -> item.file.delete()
            item.document != null -> item.document.delete()
            else -> context.contentResolver.delete(item.uri, null, null) > 0
        }
        check(deleted) { context.getString(app.myzel394.alibi.R.string.library_delete_failed) }
    }
    fun shareUri(item: RecordingFile): Uri = item.file?.let {
        FileProvider.getUriForFile(context, "${context.packageName}.files", it)
    } ?: item.uri

    private fun fromFile(file: File, video: Boolean, fragment: Boolean) = RecordingFile(
        file.toURI().toString().let(Uri::parse), file.name, file.length(), file.lastModified(), video, fragment,
        mime(file.name, video), file = file)
    private fun fromDocument(file: DocumentFile, video: Boolean, fragment: Boolean) = RecordingFile(
        file.uri, file.name ?: "", file.length(), file.lastModified(), video, fragment,
        file.type ?: mime(file.name ?: "", video), document = file)

    companion object {
        private val extensions = setOf("mp4", "3gp", "aac", "amr", "awb", "ogg", "webm", "ts", "m4a", "mp3", "raw")
        fun isMedia(name: String) = name.substringAfterLast('.', "").lowercase() in extensions
        fun isFragment(name: String) = name.substringBeforeLast('.').toLongOrNull() != null ||
            name.startsWith("alibi-recording-") || name.startsWith("fragment_")
        fun isExportName(name: String) = Regex("^\\d{4}-\\d{2}-\\d{2}T.*").matches(name)
        fun mime(name: String, video: Boolean): String = when (name.substringAfterLast('.').lowercase()) {
            "aac" -> "audio/aac"; "amr", "awb" -> "audio/amr"; "ogg" -> "audio/ogg"; "mp3" -> "audio/mpeg"
            "3gp" -> if (video) "video/3gpp" else "audio/3gpp"
            "webm" -> if (video) "video/webm" else "audio/webm"
            else -> if (video) "video/mp4" else "audio/mp4"
        }
        /** Keep previous sessions visible in the library without feeding them to the new concat. */
        fun archiveFragments(folder: BatchesFolder) {
            val stamp = System.currentTimeMillis()
            when (folder.type) {
                BatchesFolder.BatchType.INTERNAL -> folder.getInternalFolder().listFiles()?.filter {
                    it.isFile && it.nameWithoutExtension.toLongOrNull() != null
                }?.forEach { check(it.renameTo(File(it.parentFile, "fragment_${stamp}_${it.name}"))) }
                BatchesFolder.BatchType.CUSTOM -> folder.getCustomDefinedFolder().listFiles().filter {
                    it.name?.substringBeforeLast('.')?.toLongOrNull() != null
                }.forEach { check(it.renameTo("fragment_${stamp}_${it.name}")) }
                BatchesFolder.BatchType.MEDIA -> {
                    if (Build.VERSION.SDK_INT >= 29) {
                        val names = mutableListOf<Pair<Uri, String>>()
                        folder.context.contentResolver.query(folder.scopedMediaContentUri,
                            arrayOf(MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DISPLAY_NAME),
                            "${MediaStore.MediaColumns.DISPLAY_NAME} LIKE ?", arrayOf("${folder.mediaPrefix}%"), null)?.use {
                            while (it.moveToNext()) names += ContentUris.withAppendedId(folder.scopedMediaContentUri, it.getLong(0)) to it.getString(1)
                        }
                        names.forEach { (uri, name) -> check(folder.context.contentResolver.update(uri,
                            ContentValues().apply { put(MediaStore.MediaColumns.DISPLAY_NAME, "fragment_${stamp}_$name") }, null, null) > 0) }
                    } else folder.legacyMediaFolder.listFiles()?.filter { it.name.startsWith(folder.mediaPrefix) }?.forEach {
                        check(it.renameTo(File(it.parentFile, "fragment_${stamp}_${it.name}")))
                    }
                }
            }
        }
    }
}
