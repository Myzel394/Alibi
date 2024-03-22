package app.myzel394.alibi.ui.components.WelcomeScreen.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.myzel394.alibi.R
import app.myzel394.alibi.ui.BIG_PRIMARY_BUTTON_SIZE
import app.myzel394.alibi.ui.components.WelcomeScreen.atoms.TimeSelector
import app.myzel394.alibi.ui.components.atoms.MessageBox
import app.myzel394.alibi.ui.components.atoms.MessageType
import app.myzel394.alibi.ui.components.atoms.VisualDensity

@Composable
fun TimeSettingsPage(
    onContinue: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Column(
            modifier = Modifier
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                Icons.Default.AccessTime,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(128.dp),
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                stringResource(R.string.ui_welcome_timeSettings_title),
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                stringResource(R.string.ui_welcome_timeSettings_message),
                fontStyle = MaterialTheme.typography.bodySmall.fontStyle,
                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                color = MaterialTheme.typography.bodySmall.color,
            )
        }
        Spacer(modifier = Modifier.height(40.dp))
        Box(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .padding(horizontal = 16.dp)
        ) {
            TimeSelector()
        }
        Spacer(modifier = Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .widthIn(max = 400.dp)
                .padding(horizontal = 16.dp)
        ) {
            MessageBox(
                type = MessageType.INFO,
                message = stringResource(R.string.ui_welcome_timeSettings_changeableHint),
                density = VisualDensity.DENSE,
            )
        }
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = { onContinue() },
            modifier = Modifier
                .padding(16.dp)
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
