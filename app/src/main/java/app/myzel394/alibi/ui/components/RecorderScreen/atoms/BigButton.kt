package app.myzel394.alibi.ui.components.RecorderScreen.atoms

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BigButton(
    label: String,
    icon: ImageVector,
    description: String? = null,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
) {
    val orientation = LocalConfiguration.current.orientation

    BoxWithConstraints {
        val isLarge =
            maxWidth > 200.dp && maxHeight > 400.dp && orientation == Configuration.ORIENTATION_PORTRAIT

        Column(
            modifier = Modifier
                .size(if (isLarge) 250.dp else 180.dp)
                .clip(CircleShape)
                .semantics {
                    contentDescription = label
                }
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(color = MaterialTheme.colorScheme.primary),
                    onClick = onClick,
                    onLongClick = onLongClick,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier
                    .size(if (isLarge) 80.dp else 60.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(ButtonDefaults.IconSpacing))
            Text(
                label,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            if (description != null) {
                Spacer(modifier = Modifier.height(ButtonDefaults.IconSpacing))
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}