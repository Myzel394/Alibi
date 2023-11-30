package app.myzel394.alibi.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.ui.models.VideoRecorderModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("NewApi")
@Composable
fun POCVideo(
    videoRecorder: VideoRecorderModel,
    settings: AppSettings,
) {
    val context = LocalContext.current

    var started by rememberSaveable {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(64.dp)
    ) {
        Button(onClick = {
            if (!started) {
                videoRecorder.startRecording(context, settings)
            } else {
                scope.launch {
                    val information = videoRecorder.recorderService!!.getRecordingInformation()
                    val batchesFolder = videoRecorder.batchesFolder!!
                    videoRecorder.stopRecording(context)

                    batchesFolder.concatenate(
                        recordingStart = information.recordingStart,
                        extension = information.fileExtension,
                        disableCache = true,
                    )
                }
            }

            started = !started
        }) {
            Text("Start")
        }
    }
}