package app.myzel394.alibi.ui.components.CustomRecordingNotificationsScreen.molecules

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import app.myzel394.alibi.R
import app.myzel394.alibi.ui.components.CustomRecordingNotificationsScreen.atoms.PreviewIcon
import app.myzel394.alibi.ui.effects.rememberForceUpdate
import com.maxkeppeler.sheets.input.models.InputText
import java.time.Duration
import java.time.LocalDateTime
import java.time.Period

@Composable
fun EditNotificationInput(
    modifier: Modifier = Modifier,
    showOngoing: Boolean,
    title: String,
    description: String,
    icon: Painter,
    onShowOngoingChange: (Boolean) -> Unit,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onIconChange: (Int) -> Unit,
) {
    var ongoingStartTime by remember { mutableStateOf(LocalDateTime.now()) }

    val secondaryColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    LaunchedEffect(showOngoing) {
        if (showOngoing) {
            ongoingStartTime = LocalDateTime.now()
        }
    }

    Row(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(16.dp)
            .then(modifier),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val headlineSize = 22.dp

        PreviewIcon(
            modifier = Modifier.size(headlineSize),
            painter = icon,
        )

        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.height(headlineSize),
            ) {
                Text(
                    stringResource(R.string.app_name),
                    style = MaterialTheme.typography.bodySmall,
                    color = secondaryColor,
                )
                if (showOngoing) {
                    Icon(
                        Icons.Default.Circle,
                        contentDescription = null,
                        tint = secondaryColor,
                        modifier = Modifier
                            .size(8.dp)
                    )

                    val fakeAlpha = rememberForceUpdate()
                    val formattedTime = {
                        val difference =
                            Duration.between(
                                ongoingStartTime,
                                LocalDateTime.now(),
                            )
                        val minutes = difference.toMinutes()
                        val seconds = difference.minusMinutes(minutes).seconds

                        "${if (minutes < 10) "0$minutes" else minutes}:${if (seconds < 10) "0$seconds" else seconds}"
                    }
                    Text(
                        formattedTime(),
                        modifier = Modifier.alpha(fakeAlpha),
                        style = MaterialTheme.typography.bodySmall,
                        color = secondaryColor,
                    )
                }
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                BasicTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    textStyle = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurfaceVariant),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next,
                    ),
                )
                BasicTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurfaceVariant),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done,
                    ),
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    stringResource(R.string.ui_audioRecorder_action_delete_label),
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    stringResource(R.string.ui_audioRecorder_action_pause_label),
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}