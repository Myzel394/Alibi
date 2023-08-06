package app.myzel394.locationtest.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.myzel394.locationtest.dataStore
import app.myzel394.locationtest.ui.enums.Screen
import app.myzel394.locationtest.ui.screens.AudioRecorder
import app.myzel394.locationtest.ui.screens.SettingsScreen
import app.myzel394.locationtest.ui.screens.WelcomeScreen

const val SCALE_IN = 1.25f

@Composable
fun Navigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val settings = context
        .dataStore
        .data
        .collectAsState(initial = null)
        .value ?: return

    NavHost(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background),
        navController = navController,
        startDestination = if (settings.hasSeenOnboarding) Screen.AudioRecorder.route else Screen.Welcome.route,
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(navController = navController)
        }
        composable(
            Screen.AudioRecorder.route,
            enterTransition = {
                when (initialState.destination.route) {
                    Screen.Welcome.route -> null
                    else -> scaleIn(initialScale = SCALE_IN) + fadeIn()
                }
            },
            exitTransition = {
                scaleOut(targetScale = SCALE_IN) + fadeOut(tween(durationMillis = 150))
            }
        ) {
            AudioRecorder(navController = navController)
        }
        composable(
            Screen.Settings.route,
            enterTransition = {
                scaleIn(initialScale = 1 / SCALE_IN) + fadeIn()
            },
            exitTransition = {
                scaleOut(targetScale = 1 / SCALE_IN) + fadeOut(tween(durationMillis = 150))
            }
        ) {
            SettingsScreen(navController = navController)
        }
    }
}
