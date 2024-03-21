package app.myzel394.alibi.ui.components.WelcomeScreen.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.myzel394.alibi.R
import app.myzel394.alibi.ui.BIG_PRIMARY_BUTTON_SIZE
import app.myzel394.alibi.ui.components.WelcomeScreen.atoms.SaveFolderSelection

@Composable
fun SaveFolderPage(
    onBack: () -> Unit,
    onContinue: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Column(
            modifier = Modifier
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                Icons.AutoMirrored.Filled.InsertDriveFile,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(128.dp),
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                stringResource(R.string.ui_welcome_saveFolder_title),
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                stringResource(R.string.ui_welcome_saveFolder_message),
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        SaveFolderSelection()
        Spacer(modifier = Modifier.weight(1f))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(BIG_PRIMARY_BUTTON_SIZE),
            ) {
                Icon(
                    Icons.Default.ChevronLeft,
                    contentDescription = null,
                )
            }
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(BIG_PRIMARY_BUTTON_SIZE),
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
            ) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.continue_label))
            }
        }
    }
}