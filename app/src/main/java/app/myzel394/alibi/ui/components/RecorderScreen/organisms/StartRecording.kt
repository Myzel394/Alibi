package app.myzel394.alibi.ui.components.RecorderScreen.organisms

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import app.myzel394.alibi.R
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.ui.BIG_PRIMARY_BUTTON_SIZE
import app.myzel394.alibi.ui.components.RecorderScreen.molecules.AudioRecordingStart
import app.myzel394.alibi.ui.components.RecorderScreen.molecules.QuickMaxDurationSelector
import app.myzel394.alibi.ui.components.RecorderScreen.molecules.VideoRecordingStart
import app.myzel394.alibi.ui.effects.rememberForceUpdateOnLifeCycleChange
import app.myzel394.alibi.ui.models.AudioRecorderModel
import app.myzel394.alibi.ui.models.VideoRecorderModel
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun StartRecording(
    audioRecorder: AudioRecorderModel,
    videoRecorder: VideoRecorderModel,
    // Loading this from parent, because if we load it ourselves
    // and permissions have already been granted, initial
    // settings will be used, instead of the actual settings.
    appSettings: AppSettings,
    onSaveLastRecording: () -> Unit,
    onHideTopBar: () -> Unit,
    onShowTopBar: () -> Unit,
    showAudioRecorder: Boolean,
) {
    val context = LocalContext.current

    val label = stringResource(
        R.string.ui_audioRecorder_action_start_description_2,
        appSettings.maxDuration / 1000 / 60
    )
    val annotatedDescription = buildAnnotatedString {
        append(stringResource(R.string.ui_audioRecorder_action_start_description_1))

        withStyle(SpanStyle(background = MaterialTheme.colorScheme.surfaceVariant)) {
            pushStringAnnotation(
                tag = "minutes",
                annotation = label,
            )
            append(label)
        }

        append(stringResource(R.string.ui_audioRecorder_action_start_description_3))
    }

    var showQuickMaxDurationSelector by rememberSaveable {
        mutableStateOf(false)
    }

    if (showQuickMaxDurationSelector) {
        QuickMaxDurationSelector(
            onDismiss = {
                showQuickMaxDurationSelector = false
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))

        if (showAudioRecorder)
            AudioRecordingStart(
                audioRecorder = audioRecorder,
                appSettings = appSettings,
            )
        VideoRecordingStart(
            videoRecorder = videoRecorder,
            appSettings = appSettings,
            onHideAudioRecording = onHideTopBar,
            onShowAudioRecording = onShowTopBar,
            showPreview = !showAudioRecorder,
        )

        val forceUpdate = rememberForceUpdateOnLifeCycleChange()
        Column(
            modifier = Modifier
                .weight(1f)
                .then(forceUpdate),
            verticalArrangement = Arrangement.Bottom,
        ) {
            if (appSettings.lastRecording?.hasRecordingsAvailable(context) == true) {
                val label = stringResource(
                    R.string.ui_audioRecorder_action_saveOldRecording_label,
                    DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)
                        .format(appSettings.lastRecording.recordingStart),
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
                    onClick = onSaveLastRecording,
                ) {
                    Icon(
                        Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                    )
                    Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                    Text(label)
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(R.drawable.launcher_monochrome_noopacity),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant),
                        modifier = Modifier
                            .size(ButtonDefaults.IconSize)
                    )

                    Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))

                    ClickableText(
                        text = annotatedDescription,
                        onClick = { textIndex ->
                            if (annotatedDescription.getStringAnnotations(textIndex, textIndex)
                                    .firstOrNull()?.tag == "minutes"
                            ) {
                                showQuickMaxDurationSelector = true
                            }
                        },
                        modifier = Modifier
                            .widthIn(max = 300.dp)
                            .fillMaxWidth(),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }
            }
        }
    }
}