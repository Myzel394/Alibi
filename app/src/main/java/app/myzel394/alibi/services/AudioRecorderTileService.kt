package app.myzel394.alibi.services

import android.app.Activity
import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.PersistableBundle
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.compose.animation.core.updateTransition
import androidx.core.content.ContextCompat
import app.myzel394.alibi.R
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.enums.RecorderState
import app.myzel394.alibi.helpers.AudioRecorderExporter
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class AudioRecorderTileService : TileService() {
    private var job = SupervisorJob()
    private var scope = CoroutineScope(Dispatchers.IO + job)

    private var connection: ServiceConnection? = null

    override fun onStartListening() {
        super.onStartListening()
        println("started =================")

        scope.launch {
            val state = getRecorderState()
            updateTile(state)
        }
    }

    override fun onStopListening() {
        println("stoppeeeeeeeeeeeed =================")

        runCatching {
            connection?.let { unbindService(it) }
            connection = null
        }

        super.onStopListening()
    }

    private suspend fun startRecording() {
        dataStore.data.collectLatest { appSettings ->
            val notificationDetails = appSettings.notificationSettings?.let {
                RecorderNotificationHelper.NotificationDetails.fromNotificationSettings(
                    this@AudioRecorderTileService,
                    it,
                )
            }

            val intent =
                Intent(this, AudioRecorderService::class.java).apply {
                    action = "init"
                }
            ContextCompat.startForegroundService(this, intent)

            /*
            val intent =
                Intent(this@AudioRecorderTileService, AudioRecorderService::class.java).apply {
                    action = "init"

                    if (notificationDetails != null) {
                        putExtra(
                            "notificationDetails",
                            Json.encodeToString(
                                RecorderNotificationHelper.NotificationDetails.serializer(),
                                notificationDetails,
                            ),
                        )
                    }
                }
            ContextCompat.startForegroundService(this@AudioRecorderTileService, intent)

            println("recorder")
            val recorder = getRecorder()
            println("recorder eta ${recorder}")
            recorder?.startRecording()

             */
        }
    }

    override fun onClick() {
        super.onClick()
        scope.launch {
            val state = getRecorderState()

            when (state) {
                RecorderState.IDLE -> {
                    // Already update to provide a fast UI response
                    // Optimistically assume that the state will change
                    updateTile(RecorderState.RECORDING)
                    AudioRecorderExporter.clearAllRecordings(this@AudioRecorderTileService)
                    startRecording()
                }

                RecorderState.RECORDING -> {
                }

                RecorderState.PAUSED -> {
                }
            }
        }
    }

    private fun isAudioRecorderRunning(): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (AudioRecorderService::class.java.name == service.service.className) {
                return true
            }
        }

        return false
    }

    private suspend fun getRecorder(): AudioRecorderService? {
        if (!isAudioRecorderRunning()) {
            return null
        }

        val completer = CompletableDeferred<AudioRecorderService?>()

        connection = object : ServiceConnection {
            override fun onServiceConnected(p0: ComponentName?, binder: IBinder?) {
                completer.complete((binder as RecorderService.RecorderBinder).getService() as AudioRecorderService)
            }

            override fun onServiceDisconnected(p0: ComponentName?) {
                unbindService(this)
                connection = null
            }

            override fun onNullBinding(name: ComponentName?) {
                unbindService(this)
                connection = null
                completer.complete(null)
            }
        }

        bindService(
            Intent(this, AudioRecorderService::class.java),
            connection!!,
            0,
        )

        return completer.await()
    }

    private suspend fun getRecorderState(): RecorderState {
        return getRecorder()?.state ?: RecorderState.IDLE
    }

    fun updateTile(state: RecorderState) {
        when (state) {
            RecorderState.RECORDING -> {
                qsTile.label = getString(R.string.ui_audioRecorder_action_save_label)
                qsTile.state = Tile.STATE_ACTIVE
            }

            RecorderState.IDLE -> {
                qsTile.label = getString(R.string.ui_audioRecorder_action_start_label)
                qsTile.state = Tile.STATE_INACTIVE
            }

            RecorderState.PAUSED -> {

            }
        }
        println("Applying upäit naow")
        qsTile.updateTile()
    }
}