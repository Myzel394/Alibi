package app.myzel394.locationtest.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import app.myzel394.locationtest.R
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

import java.util.UUID;

const val INTERVAL_DURATION = 10000L

class RecorderService: Service() {
    private val binder = LocalBinder()
    private val handler = Handler(Looper.getMainLooper())

    private var mediaRecorder: MediaRecorder? = null
    private var onError: MediaRecorder.OnErrorListener? = null
    private var onStateChange: (RecorderState) -> Unit = {}

    private var counter = 0

    var recordingStart = mutableStateOf<LocalDateTime?>(null)
        private set
    var fileFolder: String? = null
        private set
    var bitRate: Int? = null
        private set
    var recordingState: RecorderState = RecorderState.IDLE
        private set

    val isRecording: Boolean
        get() = recordingStart.value != null

    override fun onBind(p0: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Actions.START.toString() -> {
                val fileFolder = intent.getStringExtra("fileFolder")
                val bitRate = intent.getIntExtra("bitRate", 320000)

                start(fileFolder, bitRate)
            }
            Actions.STOP.toString() -> stop()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    fun setOnErrorListener(onError: MediaRecorder.OnErrorListener) {
        this.onError = onError
    }

    fun setOnStateChangeListener(onStateChange: (RecorderState) -> Unit) {
        this.onStateChange = onStateChange
    }

    // Yield all recordings from 0 to counter
    fun getRecordingFilePaths() = sequence<String> {
        for (i in 0 until counter) {
            yield("$fileFolder/$i.${getFileExtensions()}")
        }
    }

    fun concatenateAudios(): String {
        val paths = getRecordingFilePaths().joinToString("|")
        val outputFile = "$fileFolder/concatenated.${getFileExtensions()}"
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

        return outputFile
    }

    private fun startNewRecording() {
        if (!isRecording) {
            return
        }

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
        handler.postDelayed(this::startNewRecording, INTERVAL_DURATION)
    }

    private fun start(fileFolder: String?, bitRate: Int) {
        this.fileFolder = fileFolder ?: getRandomFileFolder(this)
        this.bitRate = bitRate

        // Create folder
        File(this.fileFolder!!).mkdirs()

        println(this.fileFolder)

        recordingState = RecorderState.RECORDING
        recordingStart.value = LocalDateTime.now()

        showNotification()

        startNewRecording()
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

    private fun createRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(this)
        } else {
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFile(getFilePath())
            setOutputFormat(getOutputFormat())
            setAudioEncoder(getAudioEncoder())
            setAudioEncodingBitRate(bitRate!!)
            setAudioSamplingRate(getAudioSamplingRate())

            setOnErrorListener { mr, what, extra ->
                onError?.onError(mr, what, extra)

                this@RecorderService.stop()
            }
        }
    }

    private fun getFilePath() = "${fileFolder}/${counter}.${getFileExtensions()}"

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
        fun getOutputFormat(): Int =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                MediaRecorder.OutputFormat.AAC_ADTS
            else
                MediaRecorder.OutputFormat.THREE_GPP

        fun getFileExtensions(): String =
            when(getOutputFormat()) {
                MediaRecorder.OutputFormat.AAC_ADTS -> "aac"
                MediaRecorder.OutputFormat.THREE_GPP -> "3gp"
                else -> throw Exception("Unknown output format")
            }

        fun getAudioSamplingRate(): Int =
            when(getOutputFormat()) {
                MediaRecorder.OutputFormat.AAC_ADTS -> 96000
                MediaRecorder.OutputFormat.THREE_GPP -> 44100
                else -> throw Exception("Unknown output format")
            }

        fun getAudioEncoder(): Int =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                MediaRecorder.AudioEncoder.AAC
            else
                MediaRecorder.AudioEncoder.AMR_NB

        fun getRandomFileFolder(context: Context): String {
            // uuid
            val folder = UUID.randomUUID().toString()

            return "${context.externalCacheDir!!.absolutePath}/$folder"
        }
    }
}