package app.myzel394.alibi.services

import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaRecorder
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import app.myzel394.alibi.MainActivity
import app.myzel394.alibi.R
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.db.AudioRecorderSettings
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter.ISO_DATE_TIME
import java.util.Date

import java.util.UUID

const val AMPLITUDE_UPDATE_INTERVAL = 100L

class RecorderService: Service() {
    private val binder = LocalBinder()
    private val handler = Handler(Looper.getMainLooper())
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private var mediaRecorder: MediaRecorder? = null
    private var onError: MediaRecorder.OnErrorListener? = null
    private var onAmplitudeUpdate: () -> Unit = {}

    private var counter = 0

    lateinit var settings: Settings

    var fileFolder: String? = null
        private set
    val isRecording = mutableStateOf(false)

    val amplitudes = mutableStateListOf<Int>()

    var recordingStart: LocalDateTime? = null
        private set

    val filePaths: List<File>
        get() = File(fileFolder!!).listFiles()?.filter {
            val name = it.nameWithoutExtension

            if (name.toIntOrNull() == null) {
                return@filter false
            }

            val extension = it.extension

            extension == settings.fileExtension
        }?.toList() ?: emptyList()

    override fun onBind(p0: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Actions.START.toString() -> {
                fileFolder = getRandomFileFolder(this)

                start()
            }
            Actions.STOP.toString() -> stop()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()

        scope.cancel()
    }

    fun setOnAmplitudeUpdateListener(onAmplitudeUpdate: () -> Unit) {
        this.onAmplitudeUpdate = onAmplitudeUpdate
    }

    fun reset() {
        recordingStart = null
        counter = 0
        amplitudes.clear()
        isRecording.value = false

        File(fileFolder!!).listFiles()?.forEach {
            it.delete()
        }
    }

    private fun stripConcatenatedFileToExactDuration(
        outputFile: File
    ) {
        // Move the concatenated file to a temporary file
        val rawFile = File("$fileFolder/${outputFile.nameWithoutExtension}-raw.${settings.fileExtension}")
        outputFile.renameTo(rawFile)

        val command = "-sseof ${settings.maxDuration / -1000} -i $rawFile -y $outputFile"

        val session = FFmpegKit.execute(command)

        if (!ReturnCode.isSuccess(session.returnCode)) {
            Log.d(
                "Audio Concatenation",
                String.format(
                    "Command failed with state %s and rc %s.%s",
                    session.getState(),
                    session.getReturnCode(),
                    session.getFailStackTrace()
                )
            )

            throw Exception("Failed to strip concatenated audio")
        }
    }

    fun concatenateFiles(forceConcatenation: Boolean = false): File {
        val paths = filePaths.joinToString("|")
        val fileName = recordingStart!!
            .format(ISO_DATE_TIME)
            .toString()
            .replace(":", "-")
            .replace(".", "_")
        val outputFile = File("$fileFolder/$fileName.${settings.fileExtension}")

        if (outputFile.exists() && !forceConcatenation) {
            return outputFile
        }

        val command = "-i 'concat:$paths' -y" +
                " -acodec copy" +
                " -metadata title='$fileName' " +
                " -metadata date='${recordingStart!!.format(ISO_DATE_TIME)}'" +
                " -metadata batch_count='${filePaths.size}'" +
                " -metadata batch_duration='${settings.intervalDuration}'" +
                " -metadata max_duration='${settings.maxDuration}'" +
                " $outputFile"

        val session = FFmpegKit.execute(command)

        if (!ReturnCode.isSuccess(session.returnCode)) {
            Log.d(
                "Audio Concatenation",
                String.format(
                    "Command failed with state %s and rc %s.%s",
                    session.getState(),
                    session.getReturnCode(),
                    session.getFailStackTrace()
                )
            )

            throw Exception("Failed to concatenate audios")
        }

        val minRequiredForPossibleInExactMaxDuration = settings.maxDuration / settings.intervalDuration
        if (settings.forceExactMaxDuration && filePaths.size > minRequiredForPossibleInExactMaxDuration) {
            stripConcatenatedFileToExactDuration(outputFile)
        }

        return outputFile
    }

    private fun updateAmplitude() {
        if (!isRecording.value || mediaRecorder == null) {
            return
        }

        val amplitude = mediaRecorder!!.maxAmplitude
        amplitudes.add(amplitude)

        onAmplitudeUpdate()
        handler.postDelayed(::updateAmplitude, AMPLITUDE_UPDATE_INTERVAL)
    }

    private fun startNewRecording() {
        if (!isRecording.value) {
            return
        }

        deleteOldRecordings()

        val newRecorder = createRecorder()

        newRecorder.prepare()

        runCatching {
            mediaRecorder?.let {
                it.stop()
                it.release()
            }
        }

        newRecorder.start()
        mediaRecorder = newRecorder

        counter++

        handler.postDelayed(this::startNewRecording, settings.intervalDuration)
    }

    private fun deleteOldRecordings() {
        val timeMultiplier = settings.maxDuration / settings.intervalDuration
        val earliestCounter = counter - timeMultiplier

        File(fileFolder!!).listFiles()?.forEach { file ->
            val fileCounter = file.nameWithoutExtension.toIntOrNull() ?: return

            if (fileCounter < earliestCounter) {
                file.delete()
            }
        }
    }

    private fun createRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(this)
        } else {
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFile(getFilePath())
            setOutputFormat(settings.outputFormat)
            setAudioEncoder(settings.encoder)
            setAudioEncodingBitRate(settings.bitRate)
            setAudioSamplingRate(settings.samplingRate)

            setOnErrorListener { mr, what, extra ->
                onError?.onError(mr, what, extra)

                this@RecorderService.stop()
            }
        }
    }


    private fun start() {
        reset()
        // Create folder
        File(this.fileFolder!!).mkdirs()

        scope.launch {
            dataStore.data.collectLatest { preferenceSettings ->
                settings = Settings.from(preferenceSettings.audioRecorderSettings)
                recordingStart = LocalDateTime.now()
                isRecording.value = true

                showNotification()
                startNewRecording()
                updateAmplitude()
            }
        }
    }

    private fun stop() {
        mediaRecorder?.apply {
            runCatching {
                stop()
                release()
            }
        }
        isRecording.value = false

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun showNotification() {
        if (!isRecording.value) {
            return
        }

        val notification = NotificationCompat.Builder(this, "recorder")
            .setContentTitle("Recording Audio")
            .setContentText("Recording audio in background")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setUsesChronometer(true)
            .setChronometerCountDown(false)
            .setWhen(Date.from(recordingStart!!.atZone(ZoneId.systemDefault()).toInstant()).time)
            .setShowWhen(true)
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT,
                )
            )
            .build()

        // show notification
        startForeground(getNotificationId(), notification)
    }

    // To avoid int overflow, we'll use the number of seconds since 2023-01-01 01:01:01
    private fun getNotificationId(): Int {
        val offset = ZoneId.of("UTC").rules.getOffset(recordingStart!!)

        return (
                recordingStart!!.toEpochSecond(offset) -
                        LocalDateTime.of(2023, 1, 1, 1, 1).toEpochSecond(offset)
                ).toInt()
    }

    private fun getFilePath(): String = "$fileFolder/$counter.${settings.fileExtension}"

    inner class LocalBinder: Binder() {
        fun getService(): RecorderService = this@RecorderService
    }

    enum class Actions {
        START,
        STOP,
    }

    companion object {
        fun getRandomFileFolder(context: Context): String {
            // uuid
            val folder = UUID.randomUUID().toString()

            return "${context.externalCacheDir!!.absolutePath}/$folder"
        }

        fun startService(context: Context, connection: ServiceConnection?) {
            Intent(context, RecorderService::class.java).also { intent ->
                intent.action = Actions.START.toString()

                ContextCompat.startForegroundService(context, intent)

                if (connection != null) {
                    context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
                }
            }
        }

        fun stopService(context: Context) {
            Intent(context, RecorderService::class.java).also { intent ->
                intent.action = Actions.STOP.toString()

                context.startService(intent)
            }
        }
    }
}

data class Settings(
    val maxDuration: Long,
    val intervalDuration: Long,
    val forceExactMaxDuration: Boolean,
    val bitRate: Int,
    val samplingRate: Int,
    val outputFormat: Int,
    val encoder: Int,
) {
    val fileExtension: String
        get() = when(outputFormat) {
            MediaRecorder.OutputFormat.AAC_ADTS -> "aac"
            MediaRecorder.OutputFormat.THREE_GPP -> "3gp"
            MediaRecorder.OutputFormat.MPEG_4 -> "mp4"
            MediaRecorder.OutputFormat.MPEG_2_TS -> "ts"
            MediaRecorder.OutputFormat.WEBM -> "webm"
            MediaRecorder.OutputFormat.AMR_NB -> "amr"
            MediaRecorder.OutputFormat.AMR_WB -> "awb"
            MediaRecorder.OutputFormat.OGG -> "ogg"
            else -> "raw"
        }

    companion object {
        fun from(audioRecorderSettings: AudioRecorderSettings): Settings {
            return Settings(
                intervalDuration = audioRecorderSettings.intervalDuration,
                bitRate = audioRecorderSettings.bitRate,
                samplingRate = audioRecorderSettings.getSamplingRate(),
                outputFormat = audioRecorderSettings.getOutputFormat(),
                encoder = audioRecorderSettings.getEncoder(),
                maxDuration = audioRecorderSettings.maxDuration,
                forceExactMaxDuration = audioRecorderSettings.forceExactMaxDuration,
            )
        }
    }
}

@Composable
fun bindToRecorderService(): Pair<ServiceConnection, RecorderService?> {
    val context = LocalContext.current
    var service by remember { mutableStateOf<RecorderService?>(null) }

    val connection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                service = (binder as RecorderService.LocalBinder).getService()
            }

            override fun onServiceDisconnected(name: ComponentName?) {
            }
        }
    }

    LaunchedEffect(Unit) {
        Intent(context, RecorderService::class.java).also { intent ->
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    return connection to service
}
