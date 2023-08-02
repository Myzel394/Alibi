package app.myzel394.locationtest.ui.components

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
    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.extraLarge)
            .background(MaterialTheme.colorScheme.secondary)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            fontSize = 18.sp,
            fontWeight = FontWeight.W500,
            color = MaterialTheme.colorScheme.onSecondary,
        )
        Switch(
            colors = SwitchDefaults.colors(
                uncheckedTrackColor = MaterialTheme.colorScheme.background,
                checkedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                checkedThumbColor = MaterialTheme.colorScheme.secondary,
                uncheckedBorderColor = Color.Transparent,
                checkedBorderColor = Color.Transparent,
                disabledCheckedBorderColor = Color.Transparent,
                disabledUncheckedBorderColor = Color.Transparent,
            ),
            checked = checked,
            onCheckedChange = {
                onCheckedChange(it)
            }
        )
    }
}