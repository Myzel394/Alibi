package app.myzel394.locationtest.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.myzel394.locationtest.ui.enums.Screen
import app.myzel394.locationtest.ui.screens.AudioRecorder
import app.myzel394.locationtest.ui.screens.SettingsScreen

@Composable
fun Navigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.AudioRecorder.route) {
        composable(Screen.AudioRecorder.route) {
            AudioRecorder(
                navController = navController,
            )
        }
        composable(
            route = Screen.Settings.route
        ) {
            SettingsScreen(
                navController = navController,
            )
        }
    }
}
