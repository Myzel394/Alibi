package app.myzel394.alibi.helpers

import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.documentfile.provider.DocumentFile
import app.myzel394.alibi.helpers.MediaConverter.Companion.concatenateAudioFiles
import app.myzel394.alibi.helpers.MediaConverter.Companion.concatenateVideoFiles
import com.arthenica.ffmpegkit.FFmpegKitConfig
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
    override val concatenationFunction = ::concatenateVideoFiles
    override val ffmpegParameters = FFMPEG_PARAMETERS

    private var customFileFileDescriptor: ParcelFileDescriptor? = null

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
        }
    }

    override fun cleanup() {
        runCatching {
            customFileFileDescriptor?.close()
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

    companion object {
        fun viaInternalFolder(context: Context) = AudioBatchesFolder(context, BatchType.INTERNAL)

        fun viaCustomFolder(context: Context, folder: DocumentFile) =
            AudioBatchesFolder(context, BatchType.CUSTOM, folder)

        fun importFromFolder(folder: String, context: Context) = when (folder) {
            "_'internal" -> viaInternalFolder(context)
            else -> viaCustomFolder(context, DocumentFile.fromTreeUri(context, Uri.parse(folder))!!)
        }

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