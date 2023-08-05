package app.myzel394.locationtest.ui.screens

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import app.myzel394.locationtest.services.RecorderService
import app.myzel394.locationtest.services.bindToRecorderService
import app.myzel394.locationtest.ui.components.AudioRecorder.molecules.RecordingStatus
import app.myzel394.locationtest.ui.components.AudioRecorder.molecules.StartRecording
import app.myzel394.locationtest.ui.enums.Screen
import app.myzel394.locationtest.ui.utils.rememberFileSaverDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioRecorder(
    navController: NavController,
) {
    val context = LocalContext.current
    val saveFile = rememberFileSaverDialog("audio/aac")
    val (connection, service) = bindToRecorderService()
    val isRecording = service?.isRecording?.value ?: false

    LaunchedEffect(Unit) {
        Intent(context, RecorderService::class.java).also { intent ->
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Alibi")
                },
                actions = {
                    IconButton(
                        onClick = {
                            navController.navigate(Screen.Settings.route)
                        },
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = null
                        )
                    }
                }
            )
        },
    ) {padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (isRecording && service != null)
                RecordingStatus(service = service!!, saveFile = saveFile)
            else
                StartRecording(connection = connection, service = service)
        }
    }
}