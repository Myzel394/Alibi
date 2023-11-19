package app.myzel394.alibi.ui.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
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

@Composable
fun OnLifecycleEvent(onEvent: (owner: LifecycleOwner, event: Lifecycle.Event) -> Unit) {
    val eventHandler = rememberUpdatedState(onEvent)
    val lifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current)

    DisposableEffect(lifecycleOwner.value) {
        val lifecycle = lifecycleOwner.value.lifecycle
        val observer = LifecycleEventObserver { owner, event ->
            eventHandler.value(owner, event)
        }

        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
}

@Composable
fun rememberForceUpdateOnLifeCycleChange(
    events: Array<Lifecycle.Event> = arrayOf(
        Lifecycle.Event.ON_RESUME
    ),
): Modifier {
    var tickTack by rememberSaveable { mutableStateOf(1f) }

    OnLifecycleEvent { owner, event ->
        if (events.contains(event)) {
            tickTack = if (tickTack == 1f) 0.99f else 1f
        }
    }

    return Modifier.alpha(tickTack)
}