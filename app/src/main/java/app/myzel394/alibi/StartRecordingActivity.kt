package app.myzel394.alibi

import android.app.Activity
import android.content.Intent
import androidx.core.content.ContextCompat
import app.myzel394.alibi.services.AudioRecorderService

class StartRecordingActivity : Activity() {
    override fun onStart() {
        super.onStart()

        val context = this
        val intent = Intent(context, AudioRecorderService::class.java).apply {
            putExtra("startNow", true)
        }
        ContextCompat.startForegroundService(context, intent)

        finish()
    }
}