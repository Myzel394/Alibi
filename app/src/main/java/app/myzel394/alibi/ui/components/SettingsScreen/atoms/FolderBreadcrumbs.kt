package app.myzel394.alibi.ui.components.SettingsScreen.atoms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun FolderBreadcrumbs(
    modifier: Modifier = Modifier,
    textStyle: TextStyle? = null,
    folders: Iterable<String>,
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        folders.forEachIndexed { index, folder ->
            if (index != 0) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                )
            }
            Text(
                text = folder,
                modifier = Modifier
                    .then(modifier),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = textStyle ?: MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}