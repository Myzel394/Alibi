package app.myzel394.alibi.services

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.core.content.ContextCompat


class TempActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
    }

    override fun onStart() {
        super.onStart()

        val intent =
            Intent(this, AudioRecorderService::class.java).apply {
                action = "init"
            }
        ContextCompat.startForegroundService(this, intent)
    }
}
