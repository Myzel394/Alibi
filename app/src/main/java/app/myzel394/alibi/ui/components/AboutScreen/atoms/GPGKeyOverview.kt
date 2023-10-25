package app.myzel394.alibi.ui.components.AboutScreen.atoms

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.myzel394.alibi.R
import app.myzel394.alibi.ui.PUBLIC_KEY_FINGERPRINT
import app.myzel394.alibi.ui.PUBLIC_KEY

@Composable
fun GPGKeyOverview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(
                MaterialTheme.colorScheme.primaryContainer
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            Icons.Default.Key,
            contentDescription = null,
            modifier = Modifier.size(48.dp)
        )

        Text(
            stringResource(R.string.ui_about_gpg_key_hint),
            style = MaterialTheme.typography.bodyMedium,
        )

        val clipboardManager =
            LocalContext.current.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        Text(
            PUBLIC_KEY_FINGERPRINT,
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant
                )
                .padding(8.dp),
        )
        Button(
            onClick = {
                val clip = ClipData.newPlainText("text", PUBLIC_KEY)
                clipboardManager.setPrimaryClip(clip)
            },
            colors = ButtonDefaults.textButtonColors(),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(stringResource(R.string.ui_about_gpg_key_copy))
        }
    }
}
