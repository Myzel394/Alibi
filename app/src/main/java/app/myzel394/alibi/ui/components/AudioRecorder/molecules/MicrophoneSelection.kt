package app.myzel394.alibi.ui.components.AudioRecorder.molecules

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.myzel394.alibi.R
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.ui.components.AudioRecorder.atoms.MicrophoneSelectionButton
import app.myzel394.alibi.ui.components.AudioRecorder.atoms.MicrophoneTypeInfo
import app.myzel394.alibi.ui.components.atoms.MessageBox
import app.myzel394.alibi.ui.components.atoms.MessageType
import app.myzel394.alibi.ui.models.AudioRecorderModel
import app.myzel394.alibi.ui.utils.MicrophoneInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MicrophoneSelection(
    audioRecorder: AudioRecorderModel
) {
    val context = LocalContext.current

    var showSelection by rememberSaveable {
        mutableStateOf(false)
    }
    val sheetState = rememberModalBottomSheetState()

    val allMicrophones = MicrophoneInfo.fetchDeviceMicrophones(context)
    val visibleMicrophones = MicrophoneInfo.filterMicrophones(allMicrophones)
    val hiddenMicrophones = allMicrophones - visibleMicrophones.toSet()

    val dataStore = LocalContext.current.dataStore
    val settings = dataStore
        .data
        .collectAsState(initial = AppSettings.getDefaultInstance())
        .value

    val isTryingToReconnect =
        audioRecorder.selectedMicrophone != null && audioRecorder.microphoneStatus == AudioRecorderModel.MicrophoneConnectivityStatus.DISCONNECTED

    if (showSelection) {
        ModalBottomSheet(
            onDismissRequest = {
                showSelection = false
            },
            sheetState = sheetState,
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(48.dp),
            ) {
                Text(
                    stringResource(R.string.ui_audioRecorder_info_microphone_changeExplanation),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                )

                if (isTryingToReconnect)
                    MessageBox(
                        type = MessageType.INFO,
                        message = stringResource(
                            R.string.ui_audioRecorder_error_microphoneDisconnected_message,
                            audioRecorder.recorderService!!.selectedMicrophone?.name ?: "",
                            audioRecorder.recorderService!!.selectedMicrophone?.name ?: "",
                        )
                    )

                LazyColumn(
                    modifier = Modifier
                        .padding(horizontal = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item {
                        MicrophoneSelectionButton(
                            selected = audioRecorder.selectedMicrophone == null,
                            selectedAsFallback = isTryingToReconnect,
                            onSelect = {
                                audioRecorder.changeMicrophone(null)
                                showSelection = false
                            }
                        )
                    }

                    items(visibleMicrophones.size) {
                        val microphone = visibleMicrophones[it]

                        MicrophoneSelectionButton(
                            microphone = microphone,
                            selected = audioRecorder.selectedMicrophone == microphone,
                            onSelect = {
                                audioRecorder.changeMicrophone(microphone)
                                showSelection = false
                            }
                        )
                    }

                    if (settings.audioRecorderSettings.showAllMicrophones) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(vertical = 32.dp),
                            ) {
                                Divider(
                                    modifier = Modifier
                                        .weight(1f)
                                )
                                Text(
                                    stringResource(R.string.ui_audioRecorder_info_microphone_hiddenMicrophones),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    textAlign = TextAlign.Center,
                                )
                                Divider(
                                    modifier = Modifier
                                        .weight(1f),
                                )
                            }
                        }

                        items(hiddenMicrophones.size) {
                            val microphone = hiddenMicrophones[it]

                            MicrophoneSelectionButton(
                                microphone = microphone,
                                selected = audioRecorder.selectedMicrophone == microphone,
                                onSelect = {
                                    audioRecorder.changeMicrophone(microphone)
                                    showSelection = false
                                }
                            )
                        }
                    }
                }
            }
        }
    } else {
        // We need to show a placeholder box to keep the the rest aligned correctly
        Box {}
    }

    if (visibleMicrophones.isNotEmpty() || (settings.audioRecorderSettings.showAllMicrophones && hiddenMicrophones.isNotEmpty())) {
        Button(
            onClick = {
                showSelection = true
            },
            colors = ButtonDefaults.textButtonColors(),
        ) {
            MicrophoneTypeInfo(
                type = audioRecorder.selectedMicrophone?.type
                    ?: MicrophoneInfo.MicrophoneType.PHONE,
                modifier = Modifier.size(ButtonDefaults.IconSize),
            )
            Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
            Text(
                text = audioRecorder.selectedMicrophone.let {
                    it?.name
                        ?: stringResource(R.string.ui_audioRecorder_info_microphone_deviceMicrophone)
                }
            )
            if (isTryingToReconnect) {
                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                )
            }
        }
    }
}