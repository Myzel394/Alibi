package app.myzel394.alibi.ui.components.RecorderScreen.atoms

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import app.myzel394.alibi.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SaveButton(
    modifier: Modifier = Modifier,
    onSave: () -> Unit,
    onLongClick: () -> Unit = {},
) {
    val label = stringResource(R.string.ui_recorder_action_save_label)

    Box(
        modifier = Modifier
            .clip(ButtonDefaults.textShape)
            .semantics {
                contentDescription = label
            }
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(color = MaterialTheme.colorScheme.primary),
                onClick = onSave,
                onLongClick = onLongClick,
            )
            .padding(ButtonDefaults.TextButtonContentPadding)
            .then(modifier)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}