package app.myzel394.alibi.ui

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.myzel394.alibi.R
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.helpers.AppLockHelper
import kotlinx.coroutines.launch

// After this amount, close the app
const val MAX_TRIES = 5

// Makes sure the app needs to be unlocked first, if app lock is enabled
@Composable
fun AsLockedApp(
    content: (@Composable () -> Unit),
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val settings = context
        .dataStore
        .data
        .collectAsState(initial = null)
        .value ?: return

    // -1 = Unlocked, any other value = locked
    var tries by remember { mutableIntStateOf(0) }

    LaunchedEffect(settings.isAppLockEnabled()) {
        if (!settings.isAppLockEnabled()) {
            tries = -1
        }
    }

    if (tries == -1) {
        return content()
    }

    val title = stringResource(R.string.identityVerificationRequired_title)
    val subtitle = stringResource(R.string.identityVerificationRequired_subtitle)

    fun openAuthentication() {
        if (tries >= MAX_TRIES) {
            AppLockHelper.closeApp(context)
            return
        }

        scope.launch {
            val successful = AppLockHelper.authenticate(
                context,
                title,
                subtitle,
            ).await()

            if (successful) {
                tries = -1
                return@launch
            }

            tries++

            if (tries >= MAX_TRIES) {
                AppLockHelper.closeApp(context)
            }
        }
    }

    LaunchedEffect(settings.isAppLockEnabled()) {
        if (settings.isAppLockEnabled()) {
            openAuthentication()
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Box {}
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Icon(
                    Icons.Default.Fingerprint,
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                )
                Text(
                    text = stringResource(R.string.ui_locked_title),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(BIG_PRIMARY_BUTTON_SIZE),
                onClick = ::openAuthentication,
                colors = ButtonDefaults.filledTonalButtonColors(),
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier
                        .size(ButtonDefaults.IconSize)
                )
                Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                Text(
                    text = stringResource(R.string.ui_locked_unlocked),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}