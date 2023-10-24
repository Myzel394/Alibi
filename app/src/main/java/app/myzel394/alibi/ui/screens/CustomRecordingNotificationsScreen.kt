package app.myzel394.alibi.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import app.myzel394.alibi.R
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.db.NotificationSettings
import app.myzel394.alibi.ui.components.CustomRecordingNotificationsScreen.atoms.LandingElement
import app.myzel394.alibi.ui.components.CustomRecordingNotificationsScreen.atoms.NotificationPresetSelect
import app.myzel394.alibi.ui.components.CustomRecordingNotificationsScreen.molecules.EditNotificationInput
import app.myzel394.alibi.ui.components.CustomRecordingNotificationsScreen.organisms.NotificationEditor
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomRecordingNotificationsScreen(
    navController: NavController,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()
    )

    val dataStore = LocalContext.current.dataStore
    val settings = dataStore
        .data
        .collectAsState(initial = AppSettings.getDefaultInstance())
        .value

    var showEditor: Boolean by rememberSaveable {
        mutableStateOf(false)
    }

    LaunchedEffect(settings.notificationSettings) {
        if (settings.notificationSettings != null) {
            showEditor = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.ui_settings_option_customNotification_title))
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
        if (showEditor) {
            val scope = rememberCoroutineScope()

            NotificationEditor(
                modifier = Modifier
                    .padding(padding)
                    .padding(vertical = 16.dp),
                onNotificationChange = { notificationSettings ->
                    scope.launch {
                        dataStore.updateData { settings ->
                            settings.setNotificationSettings(notificationSettings.let {
                                if (it.preset == NotificationSettings.Preset.Default)
                                    null
                                else
                                    it
                            })
                        }
                    }
                    navController.popBackStack()
                }
            )
        } else {
            LandingElement(
                onOpenEditor = {
                    showEditor = true
                }
            )
        }
    }
}