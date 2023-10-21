package app.myzel394.alibi.ui.components.AudioRecorder.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.myzel394.alibi.ui.utils.MicrophoneInfo
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import app.myzel394.alibi.R

@Composable
fun MicrophoneSelectionButton(
    microphone: MicrophoneInfo? = null,
    selected: Boolean = false,
    onSelect: () -> Unit,
) {
    Button(
        onClick = onSelect,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        colors = if (selected) ButtonDefaults.buttonColors(
        ) else ButtonDefaults.textButtonColors(),
    ) {
        MicrophoneTypeInfo(
            type = microphone?.type ?: MicrophoneInfo.MicrophoneType.PHONE,
            modifier = Modifier.size(ButtonDefaults.IconSize),
        )
        Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
        Text(
            text = microphone?.name
                ?: stringResource(R.string.ui_audioRecorder_info_microphone_deviceMicrophone),
            fontSize = MaterialTheme.typography.bodyLarge.fontSize,
        )
    }
}
