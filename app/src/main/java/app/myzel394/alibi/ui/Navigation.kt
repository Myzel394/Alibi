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
import app.myzel394.alibi.ui.models.VideoRecorderModel
import app.myzel394.alibi.ui.screens.AboutScreen
import app.myzel394.alibi.ui.screens.CustomRecordingNotificationsScreen
import app.myzel394.alibi.ui.screens.RecorderScreen
import app.myzel394.alibi.ui.screens.SettingsScreen
import app.myzel394.alibi.ui.screens.WelcomeScreen

const val SCALE_IN = 1.25f

@Composable
fun Navigation(
    audioRecorder: AudioRecorderModel = viewModel(),
    videoRecorder: VideoRecorderModel = viewModel(),
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
        videoRecorder.bindToService(context)

        onDispose {
            audioRecorder.unbindFromService(context)
            videoRecorder.unbindFromService(context)
        }
    }

    NavHost(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background),
        navController = navController,
        startDestination = if (settings.hasSeenOnboarding) Screen.AudioRecorder.route else Screen.Welcome.route,
    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(onNavigateToAudioRecorderScreen = { navController.navigate(Screen.AudioRecorder.route) })
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
            RecorderScreen(
                onNavigateToSettingsScreen = {
                    navController.navigate(Screen.Settings.route)
                },
                audioRecorder = audioRecorder,
                videoRecorder = videoRecorder,
                settings = settings,
            )
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
            SettingsScreen(
                onBackNavigate = navController::popBackStack,
                onNavigateToCustomRecordingNotifications = {
                    navController.navigate(Screen.CustomRecordingNotifications.route)
                },
                onNavigateToAboutScreen = { navController.navigate(Screen.About.route) },
                audioRecorder = audioRecorder,
                videoRecorder = videoRecorder,
            )
        }
        composable(
            Screen.CustomRecordingNotifications.route,
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it -> it / 2 }
                ) + fadeIn()
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it -> it / 2 }
                ) + fadeOut(tween(150))
            }
        ) {
            CustomRecordingNotificationsScreen(
                onBackNavigate = navController::popBackStack
            )
        }
        composable(
            Screen.About.route,
            enterTransition = {
                scaleIn()
            },
            exitTransition = {
                scaleOut() + fadeOut(tween(150))
            }
        ) {
            AboutScreen(
                onBackNavigate = navController::popBackStack,
            )
        }
    }
}
