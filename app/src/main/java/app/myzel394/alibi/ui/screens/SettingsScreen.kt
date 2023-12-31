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
import androidx.compose.runtime.collectAsState
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
import app.myzel394.alibi.ui.components.SettingsScreen.atoms.AboutTile
import app.myzel394.alibi.ui.components.SettingsScreen.atoms.BitrateTile
import app.myzel394.alibi.ui.components.SettingsScreen.atoms.CustomNotificationTile
import app.myzel394.alibi.ui.components.SettingsScreen.atoms.DeleteRecordingsImmediatelyTile
import app.myzel394.alibi.ui.components.SettingsScreen.atoms.EncoderTile
import app.myzel394.alibi.ui.components.SettingsScreen.atoms.ImportExport
import app.myzel394.alibi.ui.components.SettingsScreen.atoms.InAppLanguagePicker
import app.myzel394.alibi.ui.components.SettingsScreen.atoms.IntervalDurationTile
import app.myzel394.alibi.ui.components.SettingsScreen.atoms.MaxDurationTile
import app.myzel394.alibi.ui.components.SettingsScreen.atoms.OutputFormatTile
import app.myzel394.alibi.ui.components.SettingsScreen.atoms.SamplingRateTile
import app.myzel394.alibi.ui.components.SettingsScreen.atoms.SaveFolderTile
import app.myzel394.alibi.ui.components.SettingsScreen.atoms.ShowAllMicrophonesTile
import app.myzel394.alibi.ui.components.SettingsScreen.atoms.ThemeSelector
import app.myzel394.alibi.ui.components.atoms.GlobalSwitch
import app.myzel394.alibi.ui.components.atoms.MessageBox
import app.myzel394.alibi.ui.components.atoms.MessageType
import app.myzel394.alibi.ui.effects.rememberSettings
import app.myzel394.alibi.ui.models.AudioRecorderModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    audioRecorder: AudioRecorderModel,
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
            if (audioRecorder.isInRecording)
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
            if (!SUPPORTS_DARK_MODE_NATIVELY) {
                ThemeSelector()
            }
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
            MaxDurationTile(settings = settings)
            IntervalDurationTile(settings = settings)
            InAppLanguagePicker()
            DeleteRecordingsImmediatelyTile(settings = settings)
            CustomNotificationTile(navController = navController, settings = settings)
            AboutTile(navController = navController)
            AnimatedVisibility(visible = settings.showAdvancedSettings) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(32.dp),
                ) {
                    Column {
                        Divider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 32.dp)
                        )
                        SaveFolderTile(settings = settings)
                        ShowAllMicrophonesTile(settings = settings)
                        BitrateTile(settings = settings)
                        SamplingRateTile(settings = settings)
                        EncoderTile(snackbarHostState = snackbarHostState, settings = settings)
                        OutputFormatTile(settings = settings)
                    }
                    Divider(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                    )
                    ImportExport(snackbarHostState = snackbarHostState)
                }
            }
        }
    }
}
