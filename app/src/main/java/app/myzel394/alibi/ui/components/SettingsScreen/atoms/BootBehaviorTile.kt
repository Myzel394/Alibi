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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import app.myzel394.alibi.R
import app.myzel394.alibi.SUPPORTED_LOCALES
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
import java.util.Locale

/*
val BOOT_BEHAVIOR_TITLE_MAP = mapOf<AppSettings.BootBehavior, Int>(
    AppSettings.BootBehavior.SHOW_NOTIFICATION to R.string.ui_settings_bootBehavior_values_SHOW_NOTIFICATION_title,
    AppSettings.BootBehavior.START_RECORDING to R.string.ui_settings_bootBehavior_values_START_RECORDING_title
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BootBehaviorTile() {
    val showDialog = rememberUseCaseState()

    ListDialog(
        state = showDialog,
        header = Header.Default(
            title = stringResource(R.string.ui_settings_bootBehavior_title),
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
                    titleText = stringResource("ui_settings_bootBehavior_values_${it.name}_title"),
                    subtitleText = stringResource("ui_settings_bootBehavior_values_${it.name}_subtitle"),
                )
            }.toList(),
            options = IntRange(0, AppSettings.BootBehavior.entries.size).map { index ->
                val locale = locales[index]!!

                ListOption(
                    titleText = locale.displayName,
                    subtitleText = locale.getDisplayName(Locale.ENGLISH),
                )
            }.toList(),
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
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(
                    locales[index]!!.toLanguageTag(),
                ),
            )
        },
    )
    SettingsTile(
        firstModifier = Modifier
            .fillMaxHeight()
            .clickable {
                showDialog.show()
            },
        title = stringResource(R.string.ui_settings_bootBehavior_title),
        leading = {
            Icon(
                Icons.Default.Smartphone,
                contentDescription = null,
            )
        },
    )
}

 */