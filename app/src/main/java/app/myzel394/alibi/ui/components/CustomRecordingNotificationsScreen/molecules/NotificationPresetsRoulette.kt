package app.myzel394.alibi.ui.components.CustomRecordingNotificationsScreen.molecules

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.myzel394.alibi.R
import app.myzel394.alibi.db.NotificationSettings
import app.myzel394.alibi.ui.components.CustomRecordingNotificationsScreen.atoms.NotificationPresetSelect

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotificationPresetsRoulette(
    onClick: (String, String, NotificationSettings.Preset) -> Unit,
) {
    val state = rememberLazyListState()

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        state = state,
        flingBehavior = rememberSnapFlingBehavior(lazyListState = state)
    ) {
        items(NotificationSettings.PRESETS.size) {
            val preset = NotificationSettings.PRESETS[it]

            val label = stringResource(
                R.string.ui_settings_customNotifications_preset_apply_label,
                stringResource(preset.titleID)
            )
            val presetTitle = stringResource(preset.titleID)
            val presetDescription = stringResource(preset.messageID)

            Box(
                modifier = Modifier.width(
                    LocalConfiguration.current.screenWidthDp.dp,
                )
            ) {
                NotificationPresetSelect(
                    modifier = Modifier
                        .fillMaxWidth(.95f)
                        .align(Alignment.Center)
                        .semantics {
                            contentDescription = label
                        }
                        .clickable {
                            onClick(
                                presetTitle,
                                presetDescription,
                                preset,
                            )
                        },
                    preset = preset,
                )
            }
        }
    }
}