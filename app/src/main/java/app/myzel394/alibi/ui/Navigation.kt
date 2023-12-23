package app.myzel394.alibi.ui

import android.content.Context
import android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG
import android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import android.hardware.biometrics.BiometricPrompt
import android.hardware.biometrics.BiometricPrompt.CryptoObject
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.CancellationSignal
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.camera.core.CameraX
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.myzel394.alibi.R
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.ui.enums.Screen
import app.myzel394.alibi.ui.models.AudioRecorderModel
import app.myzel394.alibi.ui.models.VideoRecorderModel
import app.myzel394.alibi.ui.screens.AboutScreen
import app.myzel394.alibi.ui.screens.RecorderScreen
import app.myzel394.alibi.ui.screens.CustomRecordingNotificationsScreen
import app.myzel394.alibi.ui.screens.SettingsScreen
import app.myzel394.alibi.ui.screens.WelcomeScreen
import app.myzel394.alibi.ui.utils.CameraInfo
import app.myzel394.alibi.helpers.AppLockHelper

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

    LaunchedEffect(settings.theme) {
        if (!SUPPORTS_DARK_MODE_NATIVELY) {
            val currentValue = AppCompatDelegate.getDefaultNightMode()

            if (settings.theme == AppSettings.Theme.LIGHT && currentValue != AppCompatDelegate.MODE_NIGHT_NO) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else if (settings.theme == AppSettings.Theme.DARK && currentValue != AppCompatDelegate.MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
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
                navController = navController,
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
                navController = navController,
                audioRecorder = audioRecorder,
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
                navController = navController,
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
                navController = navController,
            )
        }
    }
}
