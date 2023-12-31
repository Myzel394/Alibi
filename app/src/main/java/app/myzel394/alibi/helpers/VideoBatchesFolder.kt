package app.myzel394.alibi.helpers

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import androidx.core.net.toFile
import androidx.documentfile.provider.DocumentFile
import app.myzel394.alibi.helpers.MediaConverter.Companion.concatenateVideoFiles
import app.myzel394.alibi.ui.RECORDER_INTERNAL_SELECTED_VALUE
import app.myzel394.alibi.ui.RECORDER_MEDIA_SELECTED_VALUE
import com.arthenica.ffmpegkit.FFmpegKitConfig
import java.io.File
import java.nio.file.Paths
import java.time.LocalDateTime
import kotlin.io.path.Path

class VideoBatchesFolder(
    override val context: Context,
    override val type: BatchType,
    override val customFolder: DocumentFile? = null,
    override val subfolderName: String = ".video_recordings",
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
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
        MEDIA_RECORDINGS_SUBFOLDER,
    )

    private var customParcelFileDescriptor: ParcelFileDescriptor? = null

    override fun getOutputFileForFFmpeg(date: LocalDateTime, extension: String): String {
        return when (type) {
            BatchType.INTERNAL -> asInternalGetOutputFile(date, extension).absolutePath

            BatchType.CUSTOM -> {
                val name = getName(date, extension)

                FFmpegKitConfig.getSafParameterForWrite(
                    context,
                    (customFolder!!.findFile(name) ?: customFolder.createFile(
                        "video/${extension}",
                        getName(date, extension),
                    )!!).uri
                )!!
            }

            BatchType.MEDIA -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val mediaUri = getOrCreateMediaFile(
                        name = getName(date, extension),
                        mimeType = "video/$extension",
                        relativePath = Environment.DIRECTORY_DCIM + MEDIA_SUBFOLDER,
                    )

                    return FFmpegKitConfig.getSafParameterForWrite(
                        context,
                        mediaUri
                    )!!
                } else {
                    return Paths.get(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI.path,
                        Environment.DIRECTORY_DCIM,
                        MEDIA_SUBFOLDER,
                        getName(date, extension)
                    ).toFile()
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

    companion object {
        fun viaInternalFolder(context: Context) = VideoBatchesFolder(context, BatchType.INTERNAL)

        fun viaCustomFolder(context: Context, folder: DocumentFile) =
            VideoBatchesFolder(context, BatchType.CUSTOM, folder)

        fun viaMediaFolder(context: Context) = VideoBatchesFolder(context, BatchType.MEDIA)

        fun importFromFolder(folder: String, context: Context) = when (folder) {
            RECORDER_INTERNAL_SELECTED_VALUE -> viaInternalFolder(context)
            RECORDER_MEDIA_SELECTED_VALUE -> viaMediaFolder(context)
            else -> viaCustomFolder(
                context,
                DocumentFile.fromTreeUri(context, Uri.parse(folder))!!
            )
        }

        val MEDIA_SUBFOLDER = "/alibi"
        val MEDIA_RECORDINGS_SUBFOLDER = MEDIA_SUBFOLDER + "/video_recordings"
        val SCOPED_STORAGE_RELATIVE_PATH = Environment.DIRECTORY_DCIM + MEDIA_RECORDINGS_SUBFOLDER

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