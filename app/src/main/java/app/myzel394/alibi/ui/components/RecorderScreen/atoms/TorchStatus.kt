package app.myzel394.alibi.ui.components.RecorderScreen.atoms

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.myzel394.alibi.R

@Composable
fun TorchStatus(
    enabled: Boolean,
    onChange: () -> Unit,
) {
    Button(
        onClick = onChange,
        colors = if (enabled) ButtonDefaults.filledTonalButtonColors() else ButtonDefaults.outlinedButtonColors(),
        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
    ) {
        Icon(
            if (enabled) Icons.Default.FlashlightOff else Icons.Default.FlashlightOn,
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
        Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
        Text(
            if (enabled) stringResource(R.string.ui_videoRecorder_action_torch_off)
            else stringResource(R.string.ui_videoRecorder_action_torch_on),
        )
    }
}