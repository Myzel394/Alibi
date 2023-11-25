package app.myzel394.alibi.ui.screens

import android.content.Intent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import app.myzel394.alibi.services.VideoService

@Composable
fun POCVideo() {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val intent = Intent(context, VideoService::class.java)
        ContextCompat.startForegroundService(context, intent)
    }

    Text(text = "POCVideo")
}