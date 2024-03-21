package app.myzel394.alibi.ui.components.WelcomeScreen.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.myzel394.alibi.R
import app.myzel394.alibi.dataStore
import kotlinx.coroutines.launch

const val MINUTES_5 = 1000 * 60 * 5L
const val MINUTES_15 = 1000 * 60 * 15L
const val MINUTES_30 = 1000 * 60 * 30L
const val HOURS_1 = 1000 * 60 * 60L

@Composable
fun TimeSelector(
    modifier: Modifier = Modifier,
) {
    val OPTIONS = mapOf<Long, String>(
        MINUTES_5 to stringResource(R.string.ui_welcome_timeSettings_values_5min),
        MINUTES_15 to stringResource(R.string.ui_welcome_timeSettings_values_15min),
        MINUTES_30 to stringResource(R.string.ui_welcome_timeSettings_values_30min),
        HOURS_1 to stringResource(R.string.ui_welcome_timeSettings_values_1hour),
    )

    val scope = rememberCoroutineScope()
    val dataStore = LocalContext.current.dataStore

    var selectedDuration by rememberSaveable { mutableLongStateOf(MINUTES_15) };

    // Make sure appSettings is updated properly
    LaunchedEffect(selectedDuration) {
        scope.launch {
            dataStore.updateData {
                it.setMaxDuration(selectedDuration)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .then(modifier),
        verticalArrangement = Arrangement.Center,
    ) {
        for ((duration, label) in OPTIONS) {
            val a11yLabel = stringResource(
                R.string.ui_welcome_timeSettings_selectTime,
                label
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .semantics {
                        contentDescription = a11yLabel
                    }
                    .clickable {
                        selectedDuration = duration
                    }
                    .padding(16.dp)
            ) {
                RadioButton(
                    selected = selectedDuration == duration,
                    onClick = { selectedDuration = duration },
                )
                Text(label)
            }
        }
    }
}