package app.myzel394.alibi.ui.components.atoms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Rotate90DegreesCw
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.myzel394.alibi.R

@Composable
fun RotateDeviceToPortrait(
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 300.dp),
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Rotate90DegreesCw,
            contentDescription = stringResource(R.string.ui_rotateDevice_portrait_label),
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.tertiary,
        )
        Text(stringResource(R.string.ui_rotateDevice_portrait_label))
    }
}