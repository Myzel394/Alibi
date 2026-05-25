package app.myzel394.alibi.helpers

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.documentfile.provider.DocumentFile
import app.myzel394.alibi.db.RecordingInformation
import app.myzel394.alibi.helpers.MediaConverter.Companion.concatenateVideoFiles
import app.myzel394.alibi.ui.MEDIA_SUBFOLDER_NAME
import app.myzel394.alibi.ui.RECORDER_INTERNAL_SELECTED_VALUE
import app.myzel394.alibi.ui.RECORDER_MEDIA_SELECTED_VALUE
import app.myzel394.alibi.ui.VIDEO_RECORDING_BATCHES_SUBFOLDER_NAME
import com.arthenica.ffmpegkit.FFmpegKitConfig
import java.io.File
import java.time.LocalDateTime
import java.util.UUID

class VideoBatchesFolder(
    override val context: Context,
    override val type: BatchType,
    override val customFolder: DocumentFile? = null,
    override val subfolderName: String = VIDEO_RECORDING_BATCHES_SUBFOLDER_NAME,
) : BatchesFolder(
    context,
    type,
    customFolder,
    subfolderName,
) {
    override val concatenationFunction = ::concatenateVideoFiles
    override val ffmpegParameters = FFMPEG_PARAMETERS
    override val scopedMediaContentUri: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    override val legacyMediaFolder = File(
        Environment.getExternalStoragePublicDirectory(BASE_LEGACY_STORAGE_FOLDER),
        MEDIA_RECORDINGS_SUBFOLDER,
    )

    private var customParcelFileDescriptor: ParcelFileDescriptor? = null

    override fun getFFmpegParameters(
        recording: RecordingInformation,
        batchCounters: List<Long>,
    ): Array<String> {
        return ffmpegParameters.withOutputFormat(recording.fileExtension)
    }

    private fun Array<String>.withOutputFormat(extension: String): Array<String> =
        map {
            "$it -f $extension"
        }.toTypedArray()

    override fun prepareInputPathsForFFmpeg(extension: String): FFmpegInputPaths {
        if (type == BatchType.MEDIA && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // We can't rely on the raw on-disk path here: MediaStore may rewrite leading
            // dots in our relative path (e.g. `.video_recordings` becomes `_.video_recordings`),
            // so the file we look up via `Environment.getExternalStoragePublicDirectory(...)`
            // doesn't exist and `length() == 0` filters every batch out. Open each batch
            // through its content URI instead and hand FFmpeg the resulting file descriptor.
            val fileDescriptorPaths = getUriBatchesForFFmpeg().map {
                openUriPathForFFmpeg(it.uri, "r")
            }
            return FFmpegInputPaths(
                paths = fileDescriptorPaths.map { it.path },
                closeables = fileDescriptorPaths,
            )
        }

        if (type != BatchType.CUSTOM) {
            return super.prepareInputPathsForFFmpeg(extension)
        }

        val fileDescriptorPaths = getUriBatchesForFFmpeg().map {
            openUriPathForFFmpeg(it.uri, "r")
        }

        return FFmpegInputPaths(
            paths = fileDescriptorPaths.map { it.path },
            closeables = fileDescriptorPaths,
        )
    }

    override fun prepareOutputTargetForFFmpeg(
        date: LocalDateTime,
        extension: String,
        fileName: String,
    ): FFmpegOutputTarget {
        return when (type) {
            BatchType.CUSTOM -> prepareCustomOutputTarget(
                extension = extension,
                fileName = fileName,
            )

            BatchType.MEDIA ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    prepareMediaOutputTarget(
                        extension = extension,
                        fileName = fileName,
                    )
                } else {
                    super.prepareOutputTargetForFFmpeg(date, extension, fileName)
                }

            BatchType.INTERNAL -> super.prepareOutputTargetForFFmpeg(date, extension, fileName)
        }
    }

    private fun prepareCustomOutputTarget(
        extension: String,
        fileName: String,
    ): FFmpegOutputTarget {
        val tempFileName = ".tmp-${UUID.randomUUID()}-$fileName"
        val folder = customFolder!!
        val outputFile = folder.createFile(
            "video/$extension",
            tempFileName,
        ) ?: throw MediaConverter.FFmpegException("Unable to create export destination")
        val outputPath = openUriPathForFFmpeg(outputFile.uri, "rwt")

        return FFmpegOutputTarget(
            ffmpegPath = outputPath.path,
            closeable = outputPath,
            onCommit = {
                folder.findFile(fileName)?.delete()
                if (!outputFile.renameTo(fileName)) {
                    throw MediaConverter.FFmpegException("Unable to publish export destination")
                }

                (folder.findFile(fileName) ?: outputFile).uri.toString()
            },
            onAbort = {
                outputFile.delete()
            },
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun prepareMediaOutputTarget(
        extension: String,
        fileName: String,
    ): FFmpegOutputTarget {
        val tempFileName = "tmp-${UUID.randomUUID()}-$fileName"
        val mediaUri = createMediaFile(
            name = tempFileName,
            mimeType = "video/$extension",
            relativePath = BASE_SCOPED_STORAGE_RELATIVE_PATH + "/" + MEDIA_SUBFOLDER_NAME,
            isPending = true,
        )
        // Hand FFmpeg an FD into the MediaStore entry instead of a raw filesystem
        // path: MediaStore stores pending files with a `.pending-<id>-` prefix, so
        // the path we'd reconstruct from `relativePath + name` doesn't actually
        // exist on disk. Writing via FD also keeps the ContentResolver as the
        // single source of truth for the underlying file, so `publishPendingMediaFile`
        // is guaranteed to publish the bytes FFmpeg just wrote.
        val outputPath = openUriPathForFFmpeg(mediaUri, "rwt")

        return FFmpegOutputTarget(
            ffmpegPath = outputPath.path,
            closeable = outputPath,
            onCommit = {
                publishPendingMediaFile(mediaUri, fileName)
                mediaUri.toString()
            },
            onAbort = {
                context.contentResolver.delete(mediaUri, null, null)
            },
        )
    }

    override fun getBatchCountersForFFmpeg(): List<Long> {
        if (type == BatchType.MEDIA && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Same MediaStore path-rewrite caveat as `prepareInputPathsForFFmpeg`:
            // and read counters straight from the MediaStore query.
            return getUriBatchesForFFmpeg().map { it.counter }
        }
        return super.getBatchCountersForFFmpeg()
    }

    override fun getOutputFileForFFmpeg(
        date: LocalDateTime,
        extension: String,
        fileName: String,
    ): String {
        return when (type) {
            BatchType.INTERNAL -> asInternalGetOutputFile(fileName).absolutePath

            BatchType.CUSTOM -> {
                FFmpegKitConfig.getSafParameterForWrite(
                    context,
                    (customFolder!!.findFile(fileName) ?: customFolder.createFile(
                        "video/${extension}",
                        fileName,
                    )!!).uri
                )!!
            }

            BatchType.MEDIA -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val mediaUri = getOrCreateMediaFile(
                        name = fileName,
                        mimeType = "video/$extension",
                        relativePath = BASE_SCOPED_STORAGE_RELATIVE_PATH + "/" + MEDIA_SUBFOLDER_NAME,
                    )

                    return FFmpegKitConfig.getSafParameterForWrite(
                        context,
                        mediaUri
                    )!!
                } else {
                    val path = arrayOf(
                        Environment.getExternalStoragePublicDirectory(BASE_LEGACY_STORAGE_FOLDER),
                        MEDIA_SUBFOLDER_NAME,
                        fileName,
                    ).joinToString("/")
                    return File(path)
                        .apply {
                            createNewFile()
                        }.absolutePath
                }
            }
        }
    }

    override fun cleanup() {
        runCatching {
            customParcelFileDescriptor?.close()
        }
    }

    fun asCustomGetParcelFileDescriptor(
        counter: Long,
        fileExtension: String,
    ): ParcelFileDescriptor {
        runCatching {
            customParcelFileDescriptor?.close()
        }

        val file =
            getCustomDefinedFolder().createFile(
                "video/$fileExtension",
                "$counter.$fileExtension"
            )!!
        val resolver = context.contentResolver.acquireContentProviderClient(file.uri)!!

        resolver.use {
            customParcelFileDescriptor = it.openFile(file.uri, "w")!!

            return customParcelFileDescriptor!!
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun asMediaGetScopedStorageContentValues(name: String) = ContentValues().apply {
        put(
            MediaStore.Video.Media.IS_PENDING,
            1
        )
        put(
            MediaStore.Video.Media.RELATIVE_PATH,
            SCOPED_STORAGE_RELATIVE_PATH,
        )

        put(
            MediaStore.Video.Media.DISPLAY_NAME,
            name
        )
    }

    companion object {
        fun viaInternalFolder(context: Context) = VideoBatchesFolder(context, BatchType.INTERNAL)

        fun viaCustomFolder(context: Context, folder: DocumentFile) =
            VideoBatchesFolder(context, BatchType.CUSTOM, folder)

        fun viaMediaFolder(context: Context) = VideoBatchesFolder(context, BatchType.MEDIA)

        fun importFromFolder(folder: String?, context: Context) = when (folder) {
            null -> viaInternalFolder(context)
            RECORDER_INTERNAL_SELECTED_VALUE -> viaInternalFolder(context)
            RECORDER_MEDIA_SELECTED_VALUE -> viaMediaFolder(context)
            else -> viaCustomFolder(
                context,
                DocumentFile.fromTreeUri(context, Uri.parse(folder))!!
            )
        }

        val BASE_LEGACY_STORAGE_FOLDER = Environment.DIRECTORY_DCIM
        val MEDIA_RECORDINGS_SUBFOLDER = MEDIA_SUBFOLDER_NAME + "/.video_recordings"
        val BASE_SCOPED_STORAGE_RELATIVE_PATH = Environment.DIRECTORY_DCIM
        val SCOPED_STORAGE_RELATIVE_PATH =
            BASE_SCOPED_STORAGE_RELATIVE_PATH + "/" + MEDIA_RECORDINGS_SUBFOLDER

        // Parameters to be passed in descending order
        // Those parameters first try to concatenate without re-encoding
        // if that fails, it'll try several fallback methods
        val FFMPEG_PARAMETERS = arrayOf(
            " -c copy",
            " -c:v copy",
            " -c:v copy -c:a aac",
            " -c:v copy -c:a libmp3lame",
            " -c:v copy -c:a libopus",
            " -c:v copy -c:a libvorbis",
            " -c:a copy",
            // There's nothing else we can do to avoid re-encoding,
            // so we'll just have to re-encode the whole thing
            " -c:v libx264 -c:a copy",
            " -c:v libx264 -c:a aac",
            " -c:v libx265 -c:a aac",
            " -c:v libx264 -c:a libmp3lame",
            " -c:v libx264 -c:a libopus",
            " -c:v libx264 -c:a libvorbis",
            " -c:v libx265 -c:a copy",
            " -c:v libx265 -c:a aac",
            " -c:v libx265 -c:a libmp3lame",
            " -c:v libx265 -c:a libopus",
            " -c:v libx265 -c:a libvorbis",
        )
    }
}