package app.myzel394.alibi.ui.components.RecorderScreen.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
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
import app.myzel394.alibi.ui.SHEET_BOTTOM_OFFSET
import app.myzel394.alibi.ui.components.RecorderScreen.atoms.MicrophoneSelectionButton
import app.myzel394.alibi.ui.components.RecorderScreen.atoms.MicrophoneTypeInfo
import app.myzel394.alibi.ui.components.atoms.MessageBox
import app.myzel394.alibi.ui.components.atoms.MessageType
import app.myzel394.alibi.ui.models.AudioRecorderModel
import app.myzel394.alibi.ui.utils.MicrophoneInfo
import kotlinx.coroutines.launch

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

    val dataStore = LocalContext.current.dataStore
    val settings = dataStore
        .data
        .collectAsState(initial = AppSettings.getDefaultInstance())
        .value

    val allMicrophones = MicrophoneInfo.fetchDeviceMicrophones(context)
    val visibleMicrophones = MicrophoneInfo.filterMicrophones(allMicrophones)
    val hiddenMicrophones = allMicrophones - visibleMicrophones.toSet()

    val isTryingToReconnect =
        audioRecorder.selectedMicrophone != null && audioRecorder.microphoneStatus == AudioRecorderModel.MicrophoneConnectivityStatus.DISCONNECTED

    val shownMicrophones = if (isTryingToReconnect && visibleMicrophones.isEmpty()) {
        listOf(audioRecorder.selectedMicrophone!!)
    } else {
        visibleMicrophones
    }

    val scope = rememberCoroutineScope()
    fun hideSheet() {
        scope.launch {
            sheetState.hide()
            showSelection = false
        }
    }

    if (showSelection) {
        ModalBottomSheet(
            onDismissRequest = ::hideSheet,
            sheetState = sheetState,
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = SHEET_BOTTOM_OFFSET),
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
                            audioRecorder.selectedMicrophone?.name ?: "",
                            audioRecorder.selectedMicrophone?.name ?: "",
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
                                hideSheet()
                            }
                        )
                    }

                    items(shownMicrophones.size) {
                        val microphone = shownMicrophones[it]

                        MicrophoneSelectionButton(
                            microphone = microphone,
                            selected = audioRecorder.selectedMicrophone == microphone,
                            disabled = isTryingToReconnect && microphone == audioRecorder.selectedMicrophone,
                            onSelect = {
                                audioRecorder.changeMicrophone(microphone)
                                hideSheet()
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
                                    hideSheet()
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

    if (shownMicrophones.isNotEmpty() || (settings.audioRecorderSettings.showAllMicrophones && hiddenMicrophones.isNotEmpty())) {
        TextButton(
            onClick = {
                scope.launch {
                    showSelection = true
                    sheetState.show()
                }
            },
            contentPadding = ButtonDefaults.TextButtonWithIconContentPadding,
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