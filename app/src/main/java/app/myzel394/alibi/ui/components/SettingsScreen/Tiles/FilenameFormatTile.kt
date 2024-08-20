package app.myzel394.alibi.ui.components.SettingsScreen.Tiles

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TextSnippet
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.myzel394.alibi.R
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.ui.SHEET_BOTTOM_OFFSET
import app.myzel394.alibi.ui.components.atoms.SettingsTile
import kotlinx.coroutines.launch

val FORMAT_RESOURCE_MAP: Map<AppSettings.FilenameFormat, Int> = mapOf(
    AppSettings.FilenameFormat.DATETIME_RELATIVE_START to R.string.ui_settings_option_filenameFormat_action_relativeStart_label,
    AppSettings.FilenameFormat.DATETIME_ABSOLUTE_START to R.string.ui_settings_option_filenameFormat_action_absoluteStart_label,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilenameFormatTile(
    settings: AppSettings,
    snackbarHostState: SnackbarHostState,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val dataStore = context.dataStore

    val successMessage = stringResource(R.string.ui_settings_option_filenameFormat_success)
    fun updateValue(format: AppSettings.FilenameFormat) {
        scope.launch {
            dataStore.updateData {
                it.setFilenameFormat(format)
            }
        }
    }

    var selectionVisible by remember { mutableStateOf(false) }
    val selectionSheetState = rememberModalBottomSheetState(true)

    fun hideSheet() {
        scope.launch {
            selectionSheetState.hide()
            selectionVisible = false
        }
    }

    if (selectionVisible) {
        SelectionSheet(
            sheetState = selectionSheetState,
            updateValue = { format ->
                hideSheet()

                if (format != null) {
                    updateValue(format)

                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = successMessage,
                            duration = SnackbarDuration.Short,
                        )
                    }
                }
            },
            onDismiss = ::hideSheet,
        )
    }

    SettingsTile(
        title = stringResource(R.string.ui_settings_option_filenameFormat_title),
        description = stringResource(R.string.ui_settings_option_filenameFormat_explanation),
        leading = {
            Icon(
                Icons.AutoMirrored.Filled.TextSnippet,
                contentDescription = null,
            )
        },
        trailing = {
            Button(
                onClick = {
                    scope.launch {
                        selectionVisible = true
                    }
                },
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(
                    text = stringResource(FORMAT_RESOURCE_MAP[settings.filenameFormat]!!),
                )
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionSheet(
    sheetState: SheetState,
    updateValue: (AppSettings.FilenameFormat?) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = SHEET_BOTTOM_OFFSET)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text(
                stringResource(R.string.ui_settings_option_filenameFormat_title),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )

            SelectionButton(
                label = stringResource(R.string.ui_settings_option_filenameFormat_action_absoluteStart_label),
                explanation = stringResource(R.string.ui_settings_option_filenameFormat_action_absoluteStart_explanation),
                icon = Icons.Default.AccessTime,
                onClick = {
                    updateValue(AppSettings.FilenameFormat.DATETIME_ABSOLUTE_START)
                },
            )

            HorizontalDivider()

            SelectionButton(
                label = stringResource(R.string.ui_settings_option_filenameFormat_action_relativeStart_label),
                explanation = stringResource(R.string.ui_settings_option_filenameFormat_action_relativeStart_explanation),
                icon = Icons.Default.Timelapse,
                onClick = {
                    updateValue(AppSettings.FilenameFormat.DATETIME_RELATIVE_START)
                },
            )

            HorizontalDivider()

            SelectionButton(
                label = stringResource(R.string.ui_settings_option_filenameFormat_action_now_label),
                explanation = stringResource(R.string.ui_settings_option_filenameFormat_action_now_explanation),
                icon = Icons.Default.Circle,
                onClick = {
                    updateValue(AppSettings.FilenameFormat.DATETIME_RELATIVE_START)
                },
            )
        }
    }
}

@Composable
private fun SelectionButton(
    label: String,
    explanation: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(MaterialTheme.shapes.medium)
            .semantics {
                contentDescription = label
            }
            .clickable {
                onClick()
            }
            .padding(horizontal = 16.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier
                .size(ButtonDefaults.IconSize)
                .fillMaxWidth(0.1f),
        )
        Column(
            modifier = Modifier.fillMaxWidth(0.9f),
        ) {
            Text(label)
            Text(
                explanation,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
