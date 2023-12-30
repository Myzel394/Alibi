package app.myzel394.alibi.helpers

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import androidx.documentfile.provider.DocumentFile
import app.myzel394.alibi.helpers.MediaConverter.Companion.concatenateVideoFiles
import app.myzel394.alibi.ui.RECORDER_MEDIA_SELECTED_VALUE
import com.arthenica.ffmpegkit.FFmpegKitConfig
import java.time.LocalDateTime

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
    override val mediaContentUri: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

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
                val name = getName(date, extension)

                // Check if already exists
                var uri: Uri? = null
                context.contentResolver.query(
                    mediaContentUri,
                    arrayOf(MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DISPLAY_NAME),
                    // TODO: Improve
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
                            mediaContentUri,
                            cursor.getLong(id)
                        )
                    }
                }

                if (uri == null) {
                    // Create empty output file to be able to write to it
                    uri = context.contentResolver.insert(
                        mediaContentUri,
                        android.content.ContentValues().apply {
                            put(
                                MediaStore.MediaColumns.DISPLAY_NAME,
                                name
                            )
                            put(
                                MediaStore.MediaColumns.MIME_TYPE,
                                "video/$extension"
                            )

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                put(
                                    MediaStore.Video.Media.RELATIVE_PATH,
                                    MEDIA_RELATIVE_PATH,
                                )
                            }
                        }
                    )!!
                }

                FFmpegKitConfig.getSafParameterForWrite(
                    context,
                    uri
                )!!
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
            "_'internal" -> viaInternalFolder(context)
            RECORDER_MEDIA_SELECTED_VALUE -> viaMediaFolder(context)
            else -> viaCustomFolder(
                context,
                DocumentFile.fromTreeUri(context, Uri.parse(folder))!!
            )
        }

        val MEDIA_RELATIVE_PATH = Environment.DIRECTORY_DCIM + "/alibi/video_recordings"

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