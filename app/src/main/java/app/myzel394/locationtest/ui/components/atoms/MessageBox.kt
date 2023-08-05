package app.myzel394.locationtest.ui.components.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
fun MessageBox(
    modifier: Modifier = Modifier,
    type: MessageType,
    message: String,
    title: String? = null,
) {
    val backgroundColor = when(type) {
        MessageType.ERROR -> MaterialTheme.colorScheme.errorContainer
        MessageType.INFO -> MaterialTheme.colorScheme.tertiaryContainer
        MessageType.SUCCESS -> Color.Green.copy(alpha = 0.3f)
        MessageType.WARNING -> Color.Yellow.copy(alpha = 0.3f)
    }
    val textColor = when(type) {
        MessageType.ERROR -> MaterialTheme.colorScheme.onError
        MessageType.INFO -> MaterialTheme.colorScheme.onTertiary
        MessageType.SUCCESS -> Color.Green
        MessageType.WARNING -> Color.Yellow
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 16.dp)
            .then(modifier)
    ) {
        Icon(
            imageVector = when(type) {
                MessageType.ERROR -> Icons.Default.Error
                MessageType.INFO -> Icons.Default.Info
                MessageType.SUCCESS -> Icons.Default.Check
                MessageType.WARNING -> Icons.Default.Warning
            },
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.padding(16.dp)
        )
        Column {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor,
                )
            }
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
            )
        }
    }

}

enum class MessageType {
    ERROR,
    INFO,
    SUCCESS,
    WARNING,
}