package app.myzel394.alibi.db

import androidx.datastore.core.Serializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

class AppSettingsSerializer: Serializer<AppSettings> {
    override val defaultValue: AppSettings = AppSettings.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): AppSettings {
        return try {
            Json.decodeFromString(
                AppSettings.serializer(),
                input.readBytes().decodeToString()
            )
        } catch (error: SerializationException) {
            error.printStackTrace()
            defaultValue
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun writeTo(t: AppSettings, output: OutputStream) {
        output.write(
            Json.encodeToString(
                AppSettings.serializer(),
                t
            ).encodeToByteArray()
        )
    }
}