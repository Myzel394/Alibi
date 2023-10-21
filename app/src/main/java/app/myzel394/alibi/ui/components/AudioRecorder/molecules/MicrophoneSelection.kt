package app.myzel394.alibi.ui.components.AudioRecorder.molecules

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.myzel394.alibi.R
import app.myzel394.alibi.ui.components.AudioRecorder.atoms.MicrophoneSelectionButton
import app.myzel394.alibi.ui.components.AudioRecorder.atoms.MicrophoneTypeInfo
import app.myzel394.alibi.ui.models.AudioRecorderModel
import app.myzel394.alibi.ui.utils.MicrophoneInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MicrophoneSelection(
    microphones: List<MicrophoneInfo>,
    selectedMicrophone: MicrophoneInfo?,
    onSelect: (MicrophoneInfo?) -> Unit,
) {
    var showSelection by rememberSaveable {
        mutableStateOf(false)
    }
    val sheetState = rememberModalBottomSheetState()

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

                LazyColumn(
                    modifier = Modifier
                        .padding(horizontal = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item {
                        MicrophoneSelectionButton(
                            selected = selectedMicrophone == null,
                            onSelect = {
                                onSelect(null)
                                showSelection = false
                            }
                        )
                    }

                    items(microphones.size) {
                        val microphone = microphones[it]

                        MicrophoneSelectionButton(
                            microphone = microphone,
                            selected = selectedMicrophone == microphone,
                            onSelect = {
                                onSelect(microphone)
                                showSelection = false
                            }
                        )
                    }
                }
            }
        }
    } else {
        // We need to show a placeholder box to keep the the rest aligned correctly
        Box {}
    }

    Button(
        onClick = {
            showSelection = true
        },
        colors = ButtonDefaults.textButtonColors(),
    ) {
        MicrophoneTypeInfo(
            type = selectedMicrophone?.type
                ?: MicrophoneInfo.MicrophoneType.PHONE,
            modifier = Modifier.size(ButtonDefaults.IconSize),
        )
        Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
        Text(
            text = selectedMicrophone.let {
                it?.name
                    ?: stringResource(R.string.ui_audioRecorder_info_microphone_deviceMicrophone)
            }
        )
    }
}