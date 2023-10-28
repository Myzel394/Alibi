package app.myzel394.alibi.ui.components.SettingsScreen.atoms

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import app.myzel394.alibi.R
import app.myzel394.alibi.SUPPORTED_LOCALES
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.ui.components.atoms.SettingsTile
import app.myzel394.alibi.ui.utils.IconResource
import com.maxkeppeker.sheets.core.models.base.ButtonStyle
import com.maxkeppeker.sheets.core.models.base.Header
import com.maxkeppeker.sheets.core.models.base.IconSource
import com.maxkeppeker.sheets.core.models.base.SelectionButton
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.list.ListDialog
import com.maxkeppeler.sheets.list.models.ListOption
import com.maxkeppeler.sheets.list.models.ListSelection
import kotlinx.coroutines.launch
import java.util.Locale

val BOOT_BEHAVIOR_TITLE_MAP = mapOf<AppSettings.BootBehavior, Int>(
    AppSettings.BootBehavior.SHOW_NOTIFICATION to R.string.ui_settings_bootBehavior_values_SHOW_NOTIFICATION_title,
    AppSettings.BootBehavior.START_RECORDING to R.string.ui_settings_bootBehavior_values_START_RECORDING_title,
    AppSettings.BootBehavior.CONTINUE_RECORDING to R.string.ui_settings_bootBehavior_values_CONTINUE_RECORDING_title,
)
val BOOT_BEHAVIOR_DESCRIPTION_MAP = mapOf<AppSettings.BootBehavior, Int>(
    AppSettings.BootBehavior.SHOW_NOTIFICATION to R.string.ui_settings_bootBehavior_values_SHOW_NOTIFICATION_description,
    AppSettings.BootBehavior.START_RECORDING to R.string.ui_settings_bootBehavior_values_START_RECORDING_description,
    AppSettings.BootBehavior.CONTINUE_RECORDING to R.string.ui_settings_bootBehavior_values_CONTINUE_RECORDING_description,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BootBehaviorTile(
    settings: AppSettings,
) {
    val scope = rememberCoroutineScope()
    val showDialog = rememberUseCaseState()
    val dataStore = LocalContext.current.dataStore

    fun updateValue(behavior: AppSettings.BootBehavior?) {
        scope.launch {
            dataStore.updateData {
                it.setBootBehavior(
                    behavior
                )
            }
        }
    }

    ListDialog(
        state = showDialog,
        header = Header.Default(
            title = stringResource(R.string.ui_settings_bootBehavior_help),
            icon = IconSource(
                painter = IconResource.fromImageVector(Icons.Default.Smartphone)
                    .asPainterResource(),
                contentDescription = null,
            )
        ),
        selection = ListSelection.Single(
            showRadioButtons = true,
            options = AppSettings.BootBehavior.entries.map {
                ListOption(
                    titleText = stringResource(
                        BOOT_BEHAVIOR_TITLE_MAP[it]!!
                    ),
                    subtitleText = stringResource(
                        BOOT_BEHAVIOR_DESCRIPTION_MAP[it]!!
                    ),
                    selected = settings.bootBehavior == it,
                )
            }.toList() + listOf(
                ListOption(
                    titleText = stringResource(R.string.ui_settings_bootBehavior_values_nothing_title),
                )
            ),
            positiveButton = SelectionButton(
                icon = IconSource(
                    painter = IconResource.fromImageVector(Icons.Default.CheckCircle)
                        .asPainterResource(),
                    contentDescription = null,
                ),
                text = stringResource(android.R.string.ok),
                type = ButtonStyle.TEXT,
            )
        ) { index, _ ->
            val behavior = AppSettings.BootBehavior.values().getOrNull(index)
            updateValue(behavior)
        },
    )
    SettingsTile(
        firstModifier = Modifier
            .fillMaxHeight()
            .clickable {
                showDialog.show()
            },
        title = stringResource(R.string.ui_settings_bootBehavior_title),
        description = stringResource(
            BOOT_BEHAVIOR_TITLE_MAP[settings.bootBehavior] ?: R.string.ui_settings_bootBehavior_help
        ),
        leading = {
            Icon(
                Icons.Default.Smartphone,
                contentDescription = null,
            )
        },
    )
}
