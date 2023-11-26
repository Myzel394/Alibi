package app.myzel394.alibi.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import app.myzel394.alibi.services.OldVideoService

@Composable
fun POCVideo() {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(64.dp)
    ) {
        Button(onClick = {
            val intent = Intent(context, OldVideoService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }) {
            Text("Start")
        }
    }
}