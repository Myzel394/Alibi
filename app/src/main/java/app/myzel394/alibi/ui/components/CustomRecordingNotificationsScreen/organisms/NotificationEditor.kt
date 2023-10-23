package app.myzel394.alibi.ui.components.CustomRecordingNotificationsScreen.organisms

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.myzel394.alibi.R
import app.myzel394.alibi.db.NotificationSettings
import app.myzel394.alibi.ui.components.CustomRecordingNotificationsScreen.atoms.NotificationPresetSelect
import app.myzel394.alibi.ui.components.CustomRecordingNotificationsScreen.molecules.EditNotificationInput

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationEditor(
    modifier: Modifier = Modifier,
    scrollState: ScrollState,
) {
    val defaultTitle = stringResource(R.string.ui_audioRecorder_state_recording_title)
    val defaultDescription = stringResource(R.string.ui_audioRecorder_state_recording_description)

    var title: String by rememberSaveable {
        mutableStateOf(defaultTitle)
    }
    var description: String by rememberSaveable {
        mutableStateOf(defaultDescription)
    }
    var showOngoing: Boolean by rememberSaveable {
        mutableStateOf(true)
    }
    var icon: Int by rememberSaveable {
        mutableStateOf(R.drawable.launcher_monochrome_noopacity)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .then(modifier),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        EditNotificationInput(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            showOngoing = true,
            title = title,
            description = description,
            icon = painterResource(icon),
            onShowOngoingChange = {
                showOngoing = it
            },
            onTitleChange = {
                title = it
            },
            onDescriptionChange = {
                description = it
            },
            onIconChange = {
                icon = it
            },
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            for (preset in NotificationSettings.PRESETS) {
                val label = stringResource(
                    R.string.ui_settings_customNotifications_preset_apply_label,
                    stringResource(preset.titleID)
                )
                val presetTitle = stringResource(preset.titleID)
                val presetDescription = stringResource(preset.messageID)

                NotificationPresetSelect(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = label
                        }
                        .clickable {
                            title = presetTitle
                            description = presetDescription
                            icon = preset.iconID
                            showOngoing = preset.showOngoing
                        },
                    preset = preset,
                )
            }
        }
    }
}