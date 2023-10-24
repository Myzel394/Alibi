package app.myzel394.alibi.ui.components.CustomRecordingNotificationsScreen.organisms

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import app.myzel394.alibi.R
import app.myzel394.alibi.db.NotificationSettings
import app.myzel394.alibi.ui.components.CustomRecordingNotificationsScreen.models.NotificationViewModel
import app.myzel394.alibi.ui.components.CustomRecordingNotificationsScreen.molecules.EditNotificationInput
import app.myzel394.alibi.ui.components.CustomRecordingNotificationsScreen.molecules.NotificationPresetsRoulette

val HORIZONTAL_PADDING = 16.dp;

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NotificationEditor(
    modifier: Modifier = Modifier,
    notificationModel: NotificationViewModel = viewModel(),
    onNotificationChange: (String, String, Int, Boolean, NotificationSettings.Preset?) -> Unit,
) {
    val defaultTitle = stringResource(R.string.ui_audioRecorder_state_recording_title)
    val defaultDescription = stringResource(R.string.ui_audioRecorder_state_recording_description)

    LaunchedEffect(defaultTitle, defaultDescription) {
        notificationModel.initialize(
            defaultTitle,
            defaultDescription,
        )
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
                showOngoing = notificationModel.showOngoing,
                title = notificationModel.title,
                description = notificationModel.description,
                icon = painterResource(notificationModel.icon),
                onShowOngoingChange = {
                    notificationModel.showOngoing = it
                },
                onTitleChange = notificationModel::setTitle,
                onDescriptionChange = notificationModel::setDescription,
                onIconChange = {
                    notificationModel.icon = it
                },
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .clickable {
                        notificationModel.showOngoing = notificationModel.showOngoing.not()
                    }
                    .background(MaterialTheme.colorScheme.tertiaryContainer)
                    .padding(8.dp),
            ) {
                Checkbox(
                    checked = notificationModel.showOngoing,
                    onCheckedChange = {
                        notificationModel.showOngoing = it
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
                onClick = notificationModel::setPreset,
            )

            Button(
                onClick = {
                    onNotificationChange(
                        notificationModel.title,
                        notificationModel.description,
                        notificationModel.icon,
                        notificationModel.showOngoing,
                        notificationModel.notificationPreset,
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