package app.myzel394.locationtest.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.myzel394.locationtest.dataStore
import app.myzel394.locationtest.ui.enums.Screen
import app.myzel394.locationtest.ui.screens.AudioRecorder
import app.myzel394.locationtest.ui.screens.SettingsScreen
import app.myzel394.locationtest.ui.screens.WelcomeScreen

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
        navController = navController,
        startDestination = if (settings.hasSeenOnboarding) Screen.AudioRecorder.route else Screen.Welcome.route,
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(navController = navController)
        }
        composable(Screen.AudioRecorder.route) {
            AudioRecorder(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
    }
}
