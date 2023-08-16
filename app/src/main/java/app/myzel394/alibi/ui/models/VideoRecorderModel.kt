package app.myzel394.alibi.ui.models

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.camera.view.video.ExperimentalVideo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import app.myzel394.alibi.db.LastRecording
import app.myzel394.alibi.enums.RecorderState
import app.myzel394.alibi.services.AudioRecorderService
import app.myzel394.alibi.services.RecorderService
import app.myzel394.alibi.services.VideoRecorderService

@ExperimentalVideo class VideoRecorderModel: ViewModel() {
    var recorderState by mutableStateOf(RecorderState.IDLE)
        private set
    var recordingTime by mutableStateOf<Long?>(null)
        private set
    var amplitudes by mutableStateOf<List<Int>>(emptyList())
        private set

    var onAmplitudeChange: () -> Unit = {}

    val isInRecording: Boolean
        get() = recorderState !== RecorderState.IDLE && recordingTime != null

    val isPaused: Boolean
        get() = recorderState === RecorderState.PAUSED

    val progress: Float
        get() = 0f

    private var intent: Intent? = null
    var recorderService: VideoRecorderService? = null
        private set

    var lastRecording: LastRecording? by mutableStateOf<LastRecording?>(null)
        private set

    var onRecordingSave: () -> Unit = {}
    var onError: () -> Unit = {}

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            recorderService = null
            reset()
        }
    }

    fun reset() {
        recorderState = RecorderState.IDLE
        recordingTime = null
        amplitudes = emptyList()
    }

    fun startRecording(context: Context) {
        runCatching {
            context.unbindService(connection)
        }

        intent = Intent(context, VideoRecorderService::class.java)
        ContextCompat.startForegroundService(context, intent!!)
        context.bindService(intent!!, connection, Context.BIND_AUTO_CREATE)
    }

    fun stopRecording(context: Context, saveAsLastRecording: Boolean = true) {
        if (saveAsLastRecording) {
        }

        runCatching {
            context.unbindService(connection)
            context.stopService(intent)
        }

        reset()
    }

    fun pauseRecording() {
    }

    fun resumeRecording() {
    }

    fun setMaxAmplitudesAmount(amount: Int) {
    }

    @Composable
    fun BindToService(context: Context) {
        LaunchedEffect(Unit) {
            Intent(context, VideoRecorderService::class.java).also { intent ->
                context.bindService(intent, connection, 0)
            }
        }
    }
}
