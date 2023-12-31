package app.myzel394.alibi.helpers

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.documentfile.provider.DocumentFile
import app.myzel394.alibi.helpers.MediaConverter.Companion.concatenateAudioFiles
import app.myzel394.alibi.ui.MEDIA_SUBFOLDER_NAME
import app.myzel394.alibi.ui.RECORDER_INTERNAL_SELECTED_VALUE
import app.myzel394.alibi.ui.RECORDER_MEDIA_SELECTED_VALUE
import com.arthenica.ffmpegkit.FFmpegKitConfig
import java.io.File
import java.io.FileDescriptor
import java.time.LocalDateTime

class AudioBatchesFolder(
    override val context: Context,
    override val type: BatchType,
    override val customFolder: DocumentFile? = null,
    override val subfolderName: String = ".audio_recordings",
) : BatchesFolder(
    context,
    type,
    customFolder,
    subfolderName,
) {
    override val concatenationFunction = ::concatenateAudioFiles
    override val ffmpegParameters = FFMPEG_PARAMETERS
    override val scopedMediaContentUri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
    override val legacyMediaFolder = File(
        // TODO: Add support for `DIRECTORY_RECORDINGS`
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
        MEDIA_RECORDINGS_SUBFOLDER,
    )

    private var customFileFileDescriptor: ParcelFileDescriptor? = null
    private var mediaFileFileDescriptor: ParcelFileDescriptor? = null

    override fun getOutputFileForFFmpeg(
        date: LocalDateTime,
        extension: String,
    ): String {
        return when (type) {
            BatchType.INTERNAL -> asInternalGetOutputFile(date, extension).absolutePath

            BatchType.CUSTOM -> {
                val name = getName(date, extension)

                FFmpegKitConfig.getSafParameterForWrite(
                    context,
                    (customFolder!!.findFile(name) ?: customFolder.createFile(
                        "audio/${extension}",
                        getName(date, extension),
                    )!!).uri
                )!!
            }

            BatchType.MEDIA -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val mediaUri = getOrCreateMediaFile(
                        name = getName(date, extension),
                        mimeType = "audio/$extension",
                        relativePath = Environment.DIRECTORY_DCIM + "/" + MEDIA_SUBFOLDER_NAME,
                    )

                    return FFmpegKitConfig.getSafParameterForWrite(
                        context,
                        mediaUri
                    )!!
                } else {
                    val path = arrayOf(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                        MEDIA_SUBFOLDER_NAME,
                        getName(date, extension)
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
            customFileFileDescriptor?.close()
        }
        runCatching {
            mediaFileFileDescriptor?.close()
        }
    }

    fun asCustomGetFileDescriptor(
        counter: Long,
        fileExtension: String,
    ): FileDescriptor {
        runCatching {
            customFileFileDescriptor?.close()
        }

        val file =
            getCustomDefinedFolder().createFile("audio/$fileExtension", "$counter.$fileExtension")!!

        customFileFileDescriptor = context.contentResolver.openFileDescriptor(file.uri, "w")!!

        return customFileFileDescriptor!!.fileDescriptor
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun asMediaGetScopedStorageFileDescriptor(
        name: String,
        mimeType: String
    ): FileDescriptor {
        runCatching {
            mediaFileFileDescriptor?.close()
        }

        val mediaUri = getOrCreateMediaFile(
            name = name,
            mimeType = mimeType,
            relativePath = SCOPED_STORAGE_RELATIVE_PATH,
        )

        mediaFileFileDescriptor = context.contentResolver.openFileDescriptor(mediaUri, "w")!!

        return mediaFileFileDescriptor!!.fileDescriptor
    }

    companion object {
        fun viaInternalFolder(context: Context) = AudioBatchesFolder(context, BatchType.INTERNAL)

        fun viaCustomFolder(context: Context, folder: DocumentFile) =
            AudioBatchesFolder(context, BatchType.CUSTOM, folder)

        fun viaMediaFolder(context: Context) = AudioBatchesFolder(context, BatchType.MEDIA)

        fun importFromFolder(folder: String, context: Context) = when (folder) {
            RECORDER_INTERNAL_SELECTED_VALUE -> viaInternalFolder(context)
            RECORDER_MEDIA_SELECTED_VALUE -> viaMediaFolder(context)
            else -> viaCustomFolder(context, DocumentFile.fromTreeUri(context, Uri.parse(folder))!!)
        }

        val MEDIA_RECORDINGS_SUBFOLDER = MEDIA_SUBFOLDER_NAME + "/audio_recordings"
        val SCOPED_STORAGE_RELATIVE_PATH =
            Environment.DIRECTORY_DCIM + "/" + MEDIA_RECORDINGS_SUBFOLDER

        // Parameters to be passed in descending order
        // Those parameters first try to concatenate without re-encoding
        // if that fails, it'll try several fallback methods
        // this is audio only
        val FFMPEG_PARAMETERS = arrayOf(
            " -c copy",
            " -acodec copy",
            " -c:a aac",
            " -c:a libmp3lame",
            " -c:a libopus",
            " -c:a libvorbis",
        )
    }
}