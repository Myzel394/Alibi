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
import app.myzel394.alibi.helpers.MediaConverter.Companion.concatenateVideoFiles
import app.myzel394.alibi.ui.MEDIA_SUBFOLDER_NAME
import app.myzel394.alibi.ui.RECORDER_INTERNAL_SELECTED_VALUE
import app.myzel394.alibi.ui.RECORDER_MEDIA_SELECTED_VALUE
import app.myzel394.alibi.ui.VIDEO_RECORDING_BATCHES_SUBFOLDER_NAME
import com.arthenica.ffmpegkit.FFmpegKitConfig
import java.io.File

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

    override fun getOutputFileForFFmpeg(
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