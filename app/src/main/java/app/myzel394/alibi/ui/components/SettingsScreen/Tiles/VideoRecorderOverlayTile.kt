package app.myzel394.alibi.ui.components.SettingsScreen.Tiles

import android.Manifest
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import app.myzel394.alibi.R
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.db.VideoOverlaySettings
import app.myzel394.alibi.db.VideoOverlayTimeFormat
import app.myzel394.alibi.ui.components.atoms.PermissionRequester
import app.myzel394.alibi.ui.components.atoms.SettingsTile
import app.myzel394.alibi.ui.utils.IconResource
import com.maxkeppeker.sheets.core.models.base.Header
import com.maxkeppeker.sheets.core.models.base.IconSource
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.list.ListDialog
import com.maxkeppeler.sheets.list.models.ListOption
import com.maxkeppeler.sheets.list.models.ListSelection
import kotlinx.coroutines.launch

private val TIME_FORMAT_LABELS = mapOf(
    VideoOverlayTimeFormat.ISO_OFFSET_DATE_TIME to R.string.ui_settings_option_videoOverlay_timeFormat_isoOffset,
    VideoOverlayTimeFormat.ISO_INSTANT to R.string.ui_settings_option_videoOverlay_timeFormat_isoInstant,
    VideoOverlayTimeFormat.ISO_LOCAL_DATE_TIME to R.string.ui_settings_option_videoOverlay_timeFormat_isoLocal,
    VideoOverlayTimeFormat.DASHCAM_DATE_TIME to R.string.ui_settings_option_videoOverlay_timeFormat_dashcam,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoRecorderOverlayTile(
    settings: AppSettings,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val dataStore = context.dataStore
    val showTimeFormatDialog = rememberUseCaseState()
    val overlaySettings = settings.videoRecorderSettings.overlaySettings

    fun updateOverlaySettings(update: (VideoOverlaySettings) -> VideoOverlaySettings) {
        scope.launch {
            dataStore.updateData {
                it.setVideoRecorderSettings(
                    it.videoRecorderSettings.setOverlaySettings(
                        update(it.videoRecorderSettings.overlaySettings)
                    )
                )
            }
        }
    }

    fun updateTimeFormat(timeFormat: VideoOverlayTimeFormat) {
        updateOverlaySettings {
            it.setTimeFormat(timeFormat)
        }
    }

    ListDialog(
        state = showTimeFormatDialog,
        header = Header.Default(
            title = stringResource(R.string.ui_settings_option_videoOverlay_timeFormat_title),
            icon = IconSource(
                painter = IconResource.fromImageVector(Icons.Default.Timelapse)
                    .asPainterResource(),
                contentDescription = null,
            ),
        ),
        selection = ListSelection.Single(
            showRadioButtons = true,
            options = VideoOverlayTimeFormat.entries.map { timeFormat ->
                ListOption(
                    titleText = stringResource(TIME_FORMAT_LABELS[timeFormat]!!),
                    selected = overlaySettings.timeFormat == timeFormat,
                )
            }
        ) { index, _ ->
            updateTimeFormat(VideoOverlayTimeFormat.entries[index])
        },
    )

    Column {
        SettingsTile(
            title = stringResource(R.string.ui_settings_option_videoOverlay_time_title),
            description = stringResource(R.string.ui_settings_option_videoOverlay_time_description),
            leading = {
                Icon(
                    Icons.Default.AccessTime,
                    contentDescription = null,
                )
            },
            trailing = {
                Switch(
                    checked = overlaySettings.timeEnabled,
                    onCheckedChange = { enabled ->
                        updateOverlaySettings {
                            it.setTimeEnabled(enabled)
                        }
                    },
                )
            },
        )

        if (overlaySettings.timeEnabled) {
            SettingsTile(
                title = stringResource(R.string.ui_settings_option_videoOverlay_timeFormat_title),
                description = stringResource(R.string.ui_settings_option_videoOverlay_timeFormat_description),
                leading = {
                    Icon(
                        Icons.Default.Timelapse,
                        contentDescription = null,
                    )
                },
                trailing = {
                    Button(
                        onClick = showTimeFormatDialog::show,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        ),
                        shape = MaterialTheme.shapes.medium,
                    ) {
                        Text(stringResource(TIME_FORMAT_LABELS[overlaySettings.timeFormat]!!))
                    }
                },
            )
        }

        PermissionRequester(
            permission = Manifest.permission.ACCESS_FINE_LOCATION,
            icon = Icons.Default.GpsFixed,
            onPermissionAvailable = {
                updateOverlaySettings {
                    it.setLocationEnabled(true)
                }
            },
        ) { triggerLocationPermission ->
            SettingsTile(
                title = stringResource(R.string.ui_settings_option_videoOverlay_location_title),
                description = stringResource(R.string.ui_settings_option_videoOverlay_location_description),
                leading = {
                    Icon(
                        Icons.Default.GpsFixed,
                        contentDescription = null,
                    )
                },
                trailing = {
                    Switch(
                        checked = overlaySettings.locationEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                triggerLocationPermission()
                            } else {
                                updateOverlaySettings {
                                    it.setLocationEnabled(false)
                                }
                            }
                        },
                    )
                },
            )
        }
    }
}
