package app.myzel394.alibi.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import app.myzel394.alibi.R
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.ui.SUPPORTS_DARK_MODE_NATIVELY
import app.myzel394.alibi.ui.components.SettingsScreen.Tiles.AboutTile
import app.myzel394.alibi.ui.components.SettingsScreen.Tiles.AudioRecorderEncoderTile
import app.myzel394.alibi.ui.components.SettingsScreen.Tiles.VideoRecorderFrameRateTile
import app.myzel394.alibi.ui.components.SettingsScreen.Tiles.CustomNotificationTile
import app.myzel394.alibi.ui.components.SettingsScreen.Tiles.DeleteRecordingsImmediatelyTile
import app.myzel394.alibi.ui.components.SettingsScreen.Tiles.DividerTitle
import app.myzel394.alibi.ui.components.SettingsScreen.Tiles.VideoRecorderQualityTile
import app.myzel394.alibi.ui.components.SettingsScreen.Tiles.ImportExport
import app.myzel394.alibi.ui.components.SettingsScreen.atoms.InAppLanguagePicker
import app.myzel394.alibi.ui.components.SettingsScreen.Tiles.IntervalDurationTile
import app.myzel394.alibi.ui.components.SettingsScreen.Tiles.MaxDurationTile
import app.myzel394.alibi.ui.components.SettingsScreen.Tiles.AudioRecorderOutputFormatTile
import app.myzel394.alibi.ui.components.SettingsScreen.Tiles.AudioRecorderSamplingRateTile
import app.myzel394.alibi.ui.components.SettingsScreen.Tiles.SaveFolderTile
import app.myzel394.alibi.ui.components.SettingsScreen.Tiles.AudioRecorderShowAllMicrophonesTile
import app.myzel394.alibi.ui.components.SettingsScreen.Tiles.EnableAppLockTile
import app.myzel394.alibi.ui.components.SettingsScreen.Tiles.VideoRecorderBitrateTile
import app.myzel394.alibi.ui.components.SettingsScreen.atoms.ThemeSelector
import app.myzel394.alibi.ui.components.atoms.GlobalSwitch
import app.myzel394.alibi.ui.components.atoms.MessageBox
import app.myzel394.alibi.ui.components.atoms.MessageType
import app.myzel394.alibi.ui.effects.rememberSettings
import app.myzel394.alibi.ui.models.AudioRecorderModel
import app.myzel394.alibi.ui.models.VideoRecorderModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    audioRecorder: AudioRecorderModel,
    videoRecorder: VideoRecorderModel,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()
    )

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = {
                    Snackbar(
                        snackbarData = it,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        actionColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        actionContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        dismissActionContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            )
        },
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(stringResource(R.string.ui_settings_title))
                },
                navigationIcon = {
                    IconButton(onClick = navController::popBackStack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val scope = rememberCoroutineScope()
            val dataStore = LocalContext.current.dataStore
            val settings = rememberSettings()

            // Show alert
            if (audioRecorder.isInRecording || videoRecorder.isInRecording) {
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    MessageBox(
                        type = MessageType.WARNING,
                        title = stringResource(R.string.ui_settings_hint_recordingActive_title),
                        message = stringResource(R.string.ui_settings_hint_recordingActive_message),
                    )
                }
            }
            if (!SUPPORTS_DARK_MODE_NATIVELY) {
                ThemeSelector()
            }
            MaxDurationTile(settings = settings)
            IntervalDurationTile(settings = settings)
            InAppLanguagePicker()
            DeleteRecordingsImmediatelyTile(settings = settings)
            CustomNotificationTile(navController = navController, settings = settings)
            EnableAppLockTile(settings = settings)
            SaveFolderTile(
                settings = settings,
                snackbarHostState = snackbarHostState,
            )
            GlobalSwitch(
                label = stringResource(R.string.ui_settings_advancedSettings_label),
                checked = settings.showAdvancedSettings,
                onCheckedChange = {
                    scope.launch {
                        dataStore.updateData {
                            it.setShowAdvancedSettings(it.showAdvancedSettings.not())
                        }
                    }
                }
            )
            AnimatedVisibility(visible = settings.showAdvancedSettings) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(32.dp),
                ) {
                    Column {
                        DividerTitle(
                            title = stringResource(R.string.ui_settings_sections_audio_title),
                            description = stringResource(R.string.ui_settings_sections_audio_description),
                        )
                        AudioRecorderShowAllMicrophonesTile(settings = settings)
                        AudioRecorderSamplingRateTile(settings = settings)
                        AudioRecorderEncoderTile(
                            snackbarHostState = snackbarHostState,
                            settings = settings
                        )
                        AudioRecorderOutputFormatTile(settings = settings)

                        DividerTitle(
                            title = stringResource(R.string.ui_settings_sections_video_title),
                            description = stringResource(R.string.ui_settings_sections_video_description),
                        )
                        VideoRecorderQualityTile(settings = settings)
                        VideoRecorderBitrateTile(settings = settings)
                        VideoRecorderFrameRateTile(settings = settings)
                    }
                    Divider(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                    )
                    ImportExport(snackbarHostState = snackbarHostState)
                }
            }
            AboutTile(navController = navController)
        }
    }
}
