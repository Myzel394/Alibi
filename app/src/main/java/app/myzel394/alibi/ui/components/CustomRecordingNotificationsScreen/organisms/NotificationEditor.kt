package app.myzel394.alibi.ui.components.CustomRecordingNotificationsScreen.organisms

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.myzel394.alibi.R
import app.myzel394.alibi.db.NotificationSettings
import app.myzel394.alibi.ui.BIG_PRIMARY_BUTTON_SIZE
import app.myzel394.alibi.ui.components.CustomRecordingNotificationsScreen.atoms.NotificationPresetSelect
import app.myzel394.alibi.ui.components.CustomRecordingNotificationsScreen.molecules.EditNotificationInput
import app.myzel394.alibi.ui.components.CustomRecordingNotificationsScreen.molecules.NotificationPresetsRoulette
import kotlinx.coroutines.newFixedThreadPoolContext

val HORIZONTAL_PADDING = 16.dp;

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NotificationEditor(
    modifier: Modifier = Modifier,
    onNotificationChange: (String, String, Int, Boolean, NotificationSettings.Preset?) -> Unit,
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
        mutableIntStateOf(R.drawable.launcher_monochrome_noopacity)
    }
    var preset: NotificationSettings.Preset? by remember {
        mutableStateOf(null)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = HORIZONTAL_PADDING),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            EditNotificationInput(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                showOngoing = showOngoing,
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

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .clickable {
                        showOngoing = showOngoing.not()
                    }
                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                    .padding(8.dp),
            ) {
                Checkbox(
                    checked = showOngoing,
                    onCheckedChange = {
                        showOngoing = it
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.tertiary,
                        checkmarkColor = MaterialTheme.colorScheme.onTertiary,
                    )
                )
                Text(
                    text = stringResource(R.string.ui_settings_customNotifications_showOngoing_label),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            NotificationPresetsRoulette(
                onClick = { presetTitle, presetDescription, presetIcon, presetShowOngoing, newPreset ->
                    title = presetTitle
                    description = presetDescription
                    icon = presetIcon
                    showOngoing = presetShowOngoing
                    preset = newPreset
                }
            )

            Button(
                onClick = {
                    onNotificationChange(
                        title,
                        description,
                        icon,
                        showOngoing,
                        preset,
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = HORIZONTAL_PADDING)
                    .height(48.dp),
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier
                        .size(ButtonDefaults.IconSize)
                )
                Spacer(
                    modifier = Modifier
                        .width(ButtonDefaults.IconSpacing)
                )
                Text(
                    stringResource(R.string.ui_settings_customNotifications_save_label)
                )
            }
        }
    }
}