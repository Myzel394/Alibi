package app.myzel394.alibi.ui.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

@Composable
fun rememberForceUpdate(
    time: Long = 100L,
): Float {
    var tickTack by rememberSaveable { mutableStateOf(1f) }

    LaunchedEffect(tickTack) {
        delay(time)
        tickTack = if (tickTack == 1f) 0.99f else 1f
    }

    return tickTack
}