package app.myzel394.alibi.ui.components.AudioRecorder.molecules

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.myzel394.alibi.NotificationHelper
import app.myzel394.alibi.R
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.services.RecorderNotificationHelper
import app.myzel394.alibi.ui.BIG_PRIMARY_BUTTON_SIZE
import app.myzel394.alibi.ui.components.atoms.PermissionRequester
import app.myzel394.alibi.ui.models.AudioRecorderModel
import app.myzel394.alibi.ui.utils.rememberFileSaverDialog
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun StartRecording(
    audioRecorder: AudioRecorderModel,
    // Loading this from parent, because if we load it ourselves
    // and permissions have already been granted, initial
    // settings will be used, instead of the actual settings.
    appSettings: AppSettings
) {
    val context = LocalContext.current

    // We can't get the current `notificationDetails` inside the
    // `onPermissionAvailable` function. We'll instead use this hack
    // with `LaunchedEffect` to get the current value.
    var startRecording by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(startRecording) {
        if (startRecording) {
            startRecording = false
            audioRecorder.notificationDetails = appSettings.notificationSettings.let {
                if (it == null)
                    null
                else
                    RecorderNotificationHelper.NotificationDetails.fromNotificationSettings(
                        context,
                        it
                    )
            }
            audioRecorder.startRecording(context)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))
        PermissionRequester(
            permission = Manifest.permission.RECORD_AUDIO,
            icon = Icons.Default.Mic,
            onPermissionAvailable = {
                startRecording = true
            },
        ) { trigger ->
            val label = stringResource(R.string.ui_audioRecorder_action_start_label)
            Button(
                onClick = trigger,
                modifier = Modifier
                    .semantics {
                        contentDescription = label
                    }
                    .size(200.dp)
                    .clip(shape = CircleShape),
                colors = ButtonDefaults.outlinedButtonColors(),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp),
                    )
                    Spacer(modifier = Modifier.height(ButtonDefaults.IconSpacing))
                    Text(
                        label,
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
            }
        }
        val settings = LocalContext
            .current
            .dataStore
            .data
            .collectAsState(initial = AppSettings.getDefaultInstance())
            .value

        Text(
            stringResource(
                R.string.ui_audioRecorder_action_start_description,
                settings.audioRecorderSettings.maxDuration / 1000 / 60
            ),
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            modifier = Modifier
                .widthIn(max = 300.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        if (audioRecorder.lastRecording != null && audioRecorder.lastRecording!!.hasRecordingAvailable) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
            ) {
                val label = stringResource(
                    R.string.ui_audioRecorder_action_saveOldRecording_label,
                    DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)
                        .format(audioRecorder.lastRecording!!.recordingStart),
                )
                Button(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .height(BIG_PRIMARY_BUTTON_SIZE)
                        .semantics {
                            contentDescription = label
                        },
                    colors = ButtonDefaults.textButtonColors(),
                    onClick = {
                        audioRecorder.stopRecording(context)
                        audioRecorder.onRecordingSave()
                    },
                ) {
                    Icon(
                        Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                    )
                    Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                    Text(label)
                }
            }
        } else
            Spacer(modifier = Modifier.weight(1f))
    }
}