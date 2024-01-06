package app.myzel394.alibi.ui.components.RecorderScreen.molecules

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.myzel394.alibi.R
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.db.AudioRecorderSettings.Companion.EXAMPLE_MAX_DURATIONS
import app.myzel394.alibi.ui.SHEET_BOTTOM_OFFSET
import app.myzel394.alibi.ui.utils.formatDuration
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickMaxDurationSelector(
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val dataStore = LocalContext.current.dataStore

    val sheetState = rememberModalBottomSheetState(true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = SHEET_BOTTOM_OFFSET)
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 400.dp)
                    .padding(horizontal = 16.dp, vertical = 24.dp),
            ) {
                Text(
                    stringResource(R.string.ui_recorder_action_changeMaxDuration_title),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                )
            }

            Column {
                for (duration in EXAMPLE_MAX_DURATIONS) {
                    TextButton(
                        onClick = {
                            scope.launch {
                                sheetState.hide()
                                onDismiss()
                            }
                            scope.launch {
                                dataStore.updateData {
                                    it.setMaxDuration(duration)
                                }
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                    ) {
                        Text(formatDuration(duration))
                    }
                }
            }
        }
    }
}