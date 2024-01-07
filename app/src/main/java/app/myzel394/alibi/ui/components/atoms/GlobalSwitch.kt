package app.myzel394.alibi.ui.components.atoms

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GlobalSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.secondaryContainer.copy(
            alpha = 0.2f
        ),
        label = "backgroundColor"
    )

    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(
                backgroundColor
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            fontSize = 18.sp,
            fontWeight = FontWeight.W500,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Switch(
            colors = SwitchDefaults.colors(),
            checked = checked,
            onCheckedChange = {
                onCheckedChange(it)
            }
        )
    }
}