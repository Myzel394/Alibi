package app.myzel394.locationtest.services

import android.app.Service
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
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import app.myzel394.locationtest.R
import app.myzel394.locationtest.dataStore
import app.myzel394.locationtest.db.AudioRecorderSettings
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

import java.util.UUID;

class RecorderService: Service() {
    private val binder = LocalBinder()
    private val handler = Handler(Looper.getMainLooper())
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private var mediaRecorder: MediaRecorder? = null
    private var onError: MediaRecorder.OnErrorListener? = null
    private var onStateChange: (RecorderState) -> Unit = {}

    private var counter = 0

    lateinit var settings: Settings

    var recordingStart = mutableStateOf<LocalDateTime?>(null)
        private set
    var fileFolder: String? = null
        private set
    var recordingState: RecorderState = RecorderState.IDLE
        private set
    val isRecording: Boolean
        get() = recordingStart.value != null

    val filePaths = mutableListOf<String>()

    var originalRecordingStart: LocalDateTime? = null
        private set

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

    val progress: Float
        get() {
            val start = recordingStart.value ?: return 0f
            val now = LocalDateTime.now()
            val duration = now.toEpochSecond(ZoneId.systemDefault().rules.getOffset(now)) - start.toEpochSecond(ZoneId.systemDefault().rules.getOffset(start))

            return duration / (settings.maxDuration / 1000f)
        }

    fun setOnErrorListener(onError: MediaRecorder.OnErrorListener) {
        this.onError = onError
    }

    fun setOnStateChangeListener(onStateChange: (RecorderState) -> Unit) {
        this.onStateChange = onStateChange
    }

    fun concatenateFiles(forceConcatenation: Boolean = false): File {
        val paths = filePaths.joinToString("|")
        val outputFile = "$fileFolder/${originalRecordingStart!!.format(DateTimeFormatter.ISO_DATE_TIME)}.${settings.fileExtension}"

        if (File(outputFile).exists() && !forceConcatenation) {
            return File(outputFile)
        }

        val command = "-i \"concat:$paths\" -acodec copy $outputFile"

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
            );

            throw Exception("Failed to concatenate audios")
        }

        return File(outputFile)
    }

    private fun startNewRecording() {
        if (!isRecording) {
            return
        }

        deleteOldRecordings()

        val newRecorder = createRecorder();

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
        filePaths.add(getFilePath())

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
        filePaths.clear()
        // Create folder
        File(this.fileFolder!!).mkdirs()

        scope.launch {
            dataStore.data.collectLatest { preferenceSettings ->
                settings = Settings.from(preferenceSettings.audioRecorderSettings)
                recordingState = RecorderState.RECORDING
                recordingStart.value = LocalDateTime.now()
                originalRecordingStart = recordingStart.value

                showNotification()
                startNewRecording()
            }
        }
    }

    private fun stop() {
        recordingState = RecorderState.IDLE

        mediaRecorder?.apply {
            runCatching {
                stop()
                release()
            }
        }
        recordingStart.value = null

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun showNotification() {
        if (recordingStart.value == null) {
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
            .setWhen(Date.from(recordingStart.value!!.atZone(ZoneId.systemDefault()).toInstant()).time)
            .setShowWhen(true)
            .build()

        // show notification
        startForeground(getNotificationId(), notification)

        // call function 1 sec later
        handler.postDelayed(this::showNotification, 1000L)
    }

    // To avoid int overflow, we'll use the number of seconds since 2023-01-01 01:01:01
    private fun getNotificationId(): Int {
        val offset = ZoneId.of("UTC").rules.getOffset(recordingStart.value)

        return (
                recordingStart.value!!.toEpochSecond(offset) -
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

    enum class RecorderState {
        IDLE,
        RECORDING,
        PAUSED,
    }

    companion object {
        fun getRandomFileFolder(context: Context): String {
            // uuid
            val folder = UUID.randomUUID().toString()

            return "${context.externalCacheDir!!.absolutePath}/$folder"
        }

        fun startService(context: Context, connection: ServiceConnection?) {
            Intent(context, RecorderService::class.java).also { intent ->
                intent.action = RecorderService.Actions.START.toString()

                ContextCompat.startForegroundService(context, intent)

                if (connection != null) {
                    context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
                }
            }
        }

        fun stopService(context: Context) {
            Intent(context, RecorderService::class.java).also { intent ->
                intent.action = RecorderService.Actions.STOP.toString()

                context.startService(intent)
            }
        }
    }
}

data class Settings(
    val maxDuration: Long,
    val intervalDuration: Long,
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
            )
        }
    }
}

