package app.myzel394.alibi.ui.components.atoms

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun SettingsTile(
    modifier: Modifier = Modifier,
    firstModifier: Modifier = Modifier,
    title: String,
    description: String? = null,
    tertiaryLine: (@Composable () -> Unit) = {},
    leading: @Composable () -> Unit = {},
    trailing: @Composable () -> Unit = {},
    extra: (@Composable () -> Unit)? = null,
) {
    val content = @Composable {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .then(firstModifier)
                .fillMaxWidth()
                .padding(16.dp)
                .then(modifier),
        ) {
            leading()
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (description != null)
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                    )
                tertiaryLine()
            }
            Spacer(modifier = Modifier.width(16.dp))
            trailing()
        }
    }

    if (extra != null) {
        Column {
            content()
            extra()
        }
    } else {
        content()
    }
}