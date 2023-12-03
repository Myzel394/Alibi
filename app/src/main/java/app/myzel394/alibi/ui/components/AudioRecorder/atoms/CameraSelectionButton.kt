package app.myzel394.alibi.ui.components.AudioRecorder.atoms

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.myzel394.alibi.R
import app.myzel394.alibi.ui.utils.CameraInfo

@Composable
fun CameraSelectionButton(
    cameraID: CameraInfo.Lens,
    selected: Boolean,
    onSelected: () -> Unit,
    label: String,
    description: String? = null,
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.secondaryContainer.copy(
            alpha = 0.2f
        ) else Color.Transparent,
        // Make animation about 0.5x faster than default
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
            dampingRatio = Spring.DampingRatioNoBouncy,
        ),
        label = "backgroundColor"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onSelected)
            .background(backgroundColor)
            .padding(vertical = 8.dp, horizontal = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(
                selected = selected,
                onClick = onSelected,
            )
            if (description == null) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelLarge,
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        label,
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        description,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
        Icon(
            CAMERA_LENS_ICON_MAP[cameraID]!!,
            contentDescription = null,
            modifier = Modifier
                .size(24.dp),
        )
    }
}

val CAMERA_LENS_ICON_MAP = mapOf(
    CameraInfo.Lens.BACK to Icons.Default.Camera,
    CameraInfo.Lens.FRONT to Icons.Default.Person,
    CameraInfo.Lens.EXTERNAL to Icons.Default.Videocam,
)
