package app.myzel394.locationtest.ui.components.AudioRecorder.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.myzel394.locationtest.services.RecorderService
import app.myzel394.locationtest.ui.BIG_PRIMARY_BUTTON_SIZE
import app.myzel394.locationtest.ui.components.atoms.Pulsating
import app.myzel394.locationtest.ui.utils.formatDuration
import app.myzel394.locationtest.ui.utils.rememberFileSaverDialog
import kotlinx.coroutines.delay
import java.io.File
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId

@Composable
fun RecordingStatus(
    service: RecorderService,
    saveFile: (File) -> Unit,
) {
    val context = LocalContext.current

    var now by remember { mutableStateOf(LocalDateTime.now()) }

    val start = service.recordingStart!!
    val duration = now.toEpochSecond(ZoneId.systemDefault().rules.getOffset(now)) - start.toEpochSecond(ZoneId.systemDefault().rules.getOffset(start))
    val progress = duration / (service.settings.maxDuration / 1000f)

    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(1000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Box {}
        RealtimeAudioVisualizer(service = service)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                val distance = Duration.between(service.recordingStart, now).toMillis()

                Pulsating {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = formatDuration(distance),
                    style = MaterialTheme.typography.headlineLarge,
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .width(300.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))

            var showDeleteDialog by remember { mutableStateOf(false) }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showDeleteDialog = false
                    },
                    title = {
                        Text("Delete Recording?")
                    },
                    text = {
                        Text("Are you sure you want to delete this recording?")
                    },
                    icon = {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                        )
                    },
                    confirmButton = {
                        Button(
                            modifier = Modifier
                                .semantics {
                                    contentDescription = "Confirm Recording Deletion"
                                },
                            onClick = {
                                showDeleteDialog = false
                                RecorderService.stopService(context)
                                service.reset()
                            },
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(ButtonDefaults.IconSize),
                            )
                            Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                            Text("Delete")
                        }
                    },
                    dismissButton = {
                        Button(
                            modifier = Modifier
                                .semantics {
                                    contentDescription = "Cancel Recording Deletion"
                                },
                            onClick = {
                                showDeleteDialog = false
                            },
                            colors = ButtonDefaults.textButtonColors(),
                        ) {
                            Icon(
                                Icons.Default.Cancel,
                                contentDescription = null,
                                modifier = Modifier.size(ButtonDefaults.IconSize),
                            )
                            Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                            Text("Cancel")
                        }
                    }
                )
            }
            Button(
                modifier = Modifier
                    .semantics {
                        contentDescription = "Delete Recording"
                    },
                onClick = {
                    showDeleteDialog = true
                },
                colors = ButtonDefaults.textButtonColors(),
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                )
                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                Text("Delete")
            }
        }
        Button(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .height(BIG_PRIMARY_BUTTON_SIZE)
                .semantics {
                    contentDescription = "Save Recording"
                },
            onClick = {
                RecorderService.stopService(context)

                saveFile(service.concatenateFiles())
            },
        ) {
            Icon(
                Icons.Default.Save,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize),
            )
            Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
            Text("Save Recording")
        }
    }
}