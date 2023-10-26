package app.myzel394.alibi.ui.components.AudioRecorder.atoms

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MicExternalOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.myzel394.alibi.ui.utils.MicrophoneInfo
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import app.myzel394.alibi.R
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.db.AppSettings

@Composable
fun MicrophoneSelectionButton(
    microphone: MicrophoneInfo? = null,
    selected: Boolean = false,
    selectedAsFallback: Boolean = false,
    disabled: Boolean = false,
    onSelect: () -> Unit,
) {
    val dataStore = LocalContext.current.dataStore
    val settings = dataStore
        .data
        .collectAsState(initial = AppSettings.getDefaultInstance())
        .value

    // Copied from Android's [FilledButtonTokens]
    val disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)

    Button(
        onClick = onSelect,
        enabled = !disabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        colors = if (selected) ButtonDefaults.buttonColors() else ButtonDefaults.textButtonColors(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ButtonDefaults.IconSpacing),
        ) {
            MicrophoneTypeInfo(
                type = microphone?.type ?: MicrophoneInfo.MicrophoneType.PHONE,
                modifier = Modifier.size(ButtonDefaults.IconSize),
            )
            Column {
                Text(
                    text = microphone?.name
                        ?: stringResource(R.string.ui_audioRecorder_info_microphone_deviceMicrophone),
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && settings.audioRecorderSettings.showAllMicrophones && microphone?.deviceInfo?.address?.isNotBlank() == true)
                    Text(
                        microphone.deviceInfo.address.toString(),
                        fontSize = MaterialTheme.typography.bodySmall.toSpanStyle().fontSize,
                        color = if (disabled) disabledTextColor else if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.secondary,
                    )
            }
            if (selectedAsFallback)
                Icon(
                    Icons.Default.MicExternalOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier
                        .size(ButtonDefaults.IconSize),
                )
        }
    }
}
