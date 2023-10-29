package app.myzel394.alibi.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.ui.enums.Screen
import app.myzel394.alibi.ui.models.AudioRecorderModel
import app.myzel394.alibi.ui.screens.AboutScreen
import app.myzel394.alibi.ui.screens.AudioRecorderScreen
import app.myzel394.alibi.ui.screens.CustomRecordingNotificationsScreen
import app.myzel394.alibi.ui.screens.SettingsScreen
import app.myzel394.alibi.ui.screens.WelcomeScreen

const val SCALE_IN = 1.25f

@Composable
fun Navigation(
    audioRecorder: AudioRecorderModel = viewModel()
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val settings = context
        .dataStore
        .data
        .collectAsState(initial = null)
        .value ?: return

    DisposableEffect(Unit) {
        audioRecorder.bindToService(context)

        onDispose {
            audioRecorder.unbindFromService(context)
        }
    }

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
        ) {
            AudioRecorderScreen(
                navController = navController,
                audioRecorder = audioRecorder,
            )
        }
        composable(
            Screen.Settings.route,
        ) {
            SettingsScreen(
                navController = navController,
                audioRecorder = audioRecorder,
            )
        }
        composable(
            Screen.CustomRecordingNotifications.route,
        ) {
            CustomRecordingNotificationsScreen(
                navController = navController,
            )
        }
        composable(
            Screen.About.route,
        ) {
            AboutScreen(
                navController = navController,
            )
        }
    }
}
