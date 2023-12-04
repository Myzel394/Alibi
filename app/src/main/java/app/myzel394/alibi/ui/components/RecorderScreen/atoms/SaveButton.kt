package app.myzel394.alibi.ui.components.RecorderScreen.atoms

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import app.myzel394.alibi.R

@Composable
fun SaveButton(
    modifier: Modifier = Modifier,
    onSave: () -> Unit,
) {
    val label = stringResource(R.string.ui_audioRecorder_action_save_label)

    Button(
        modifier = Modifier
            .semantics {
                contentDescription = label
            }
            .then(modifier),
        onClick = onSave,
        colors = ButtonDefaults.textButtonColors(),
    ) {
        Icon(
            Icons.Default.Save,
            contentDescription = null,
            modifier = Modifier
                .size(ButtonDefaults.IconSize)
        )
        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
        Text(
            label,
            fontSize = MaterialTheme.typography.bodySmall.fontSize,
        )
    }
}