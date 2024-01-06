package app.myzel394.alibi.ui.components.RecorderScreen.atoms

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
    val label = stringResource(R.string.ui_recorder_action_save_label)

    TextButton(
        modifier = Modifier
            .semantics {
                contentDescription = label
            }
            .then(modifier),
        onClick = onSave,
    ) {
        Text(
            label,
            fontSize = MaterialTheme.typography.bodySmall.fontSize,
        )
    }
}