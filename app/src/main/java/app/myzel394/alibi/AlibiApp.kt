package app.myzel394.alibi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.myzel394.alibi.ui.AsLockedApp
import app.myzel394.alibi.ui.LockedAppHandlers
import app.myzel394.alibi.ui.Navigation
import app.myzel394.alibi.ui.theme.AlibiTheme

@Composable
fun AlibiApp() {
    AlibiTheme {
        LockedAppHandlers()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    MaterialTheme.colorScheme.background
                )
        ) {
            AsLockedApp {
                Navigation()
            }
        }
    }
}