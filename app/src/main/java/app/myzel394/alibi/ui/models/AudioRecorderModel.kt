package app.myzel394.alibi.ui.models

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import app.myzel394.alibi.enums.RecorderState
import app.myzel394.alibi.services.AudioRecorderService
import app.myzel394.alibi.services.RecorderService

class AudioRecorderModel: ViewModel() {
    var recorderState by mutableStateOf(RecorderState.IDLE)
        private set
    var recordingTime by mutableStateOf<Long?>(null)
        private set
    var amplitudes by mutableStateOf<List<Int>?>(null)
        private set

    private var intent: Intent? = null
    private var recorderService: RecorderService? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            recorderService = ((service as RecorderService.RecorderBinder).getService() as AudioRecorderService).also {recorder ->
                recorder.onStateChange = { state ->
                    recorderState = state
                }
                recorder.onRecordingTimeChange = { time ->
                    recordingTime = time
                }
                recorder.onAmplitudeChange = { amps ->
                    amplitudes = amps
                }
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            recorderService = null
            reset()
        }
    }

    fun reset() {
        recorderState = RecorderState.IDLE
        recordingTime = null
        amplitudes = null
    }

    fun startRecording(context: Context) {
        runCatching {
            context.unbindService(connection)
        }

        val intent = Intent(context, AudioRecorderService::class.java)
        ContextCompat.startForegroundService(context, intent)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    fun stopRecording(context: Context) {
        context.stopService(intent)
        context.unbindService(connection)
    }
}