package app.myzel394.locationtest.ui.components.SettingsScreen.atoms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.myzel394.locationtest.dataStore
import app.myzel394.locationtest.db.AppSettings
import app.myzel394.locationtest.db.AudioRecorderSettings
import app.myzel394.locationtest.ui.components.atoms.SettingsTile
import app.myzel394.locationtest.ui.utils.formatDuration
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.duration.DurationDialog
import com.maxkeppeler.sheets.duration.models.DurationConfig
import com.maxkeppeler.sheets.duration.models.DurationFormat
import com.maxkeppeler.sheets.duration.models.DurationSelection
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntervalDurationTile() {
    val scope = rememberCoroutineScope()
    val showDurationPicker = rememberUseCaseState()
    val dataStore = LocalContext.current.dataStore
    val settings = dataStore
        .data
        .collectAsState(initial = AppSettings.getDefaultInstance())
        .value

    DurationDialog(
        state = showDurationPicker,
        selection = DurationSelection { newTimeInSeconds ->
            scope.launch {
                dataStore.updateData {
                    it.setAudioRecorderSettings(
                        it.audioRecorderSettings.setIntervalDuration(newTimeInSeconds * 1000L)
                    )
                }
            }
        },
        config = DurationConfig(
            timeFormat = DurationFormat.MM_SS,
            currentTime = settings.audioRecorderSettings.intervalDuration / 1000,
            minTime = 10,
            maxTime = 60 * 60,
        )
    )
    SettingsTile(
        title = "Batch duration",
        description = "Record a single batch for this duration. Alibi records multiple batches and deletes the oldest one. When exporting the audio, all batches will be merged together",
        leading = {
            Icon(
                Icons.Default.Mic,
                contentDescription = null,
            )
        },
        trailing = {
            Button(
                onClick = showDurationPicker::show,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
                shape = MaterialTheme.shapes.medium,
            ) {
                Text(
                    text = formatDuration(settings.audioRecorderSettings.intervalDuration),
                )
            }
        },
        extra = {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(
                    horizontal = 32.dp,
                ),
            ) {
                items(AudioRecorderSettings.EXAMPLE_DURATION_TIMES.size) {
                    val duration = AudioRecorderSettings.EXAMPLE_DURATION_TIMES[it]

                    Button(
                        onClick = {
                            scope.launch {
                                dataStore.updateData {
                                    it.setAudioRecorderSettings(
                                        it.audioRecorderSettings.setIntervalDuration(duration)
                                    )
                                }
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(),
                        shape = ButtonDefaults.textShape,
                        contentPadding = ButtonDefaults.TextButtonContentPadding,
                    ) {
                        Text(
                            text = formatDuration(duration),
                        )
                    }
                }
            }
        }
    )
}