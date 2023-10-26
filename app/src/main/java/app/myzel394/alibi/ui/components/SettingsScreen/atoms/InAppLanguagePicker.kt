package app.myzel394.alibi.ui.components.SettingsScreen.atoms

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import app.myzel394.alibi.R
import app.myzel394.alibi.SUPPORTED_LOCALES
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.db.AudioRecorderSettings
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InAppLanguagePicker() {
    val showDialog = rememberUseCaseState()
    val locales = Locale.getAvailableLocales().filter {
        val locale = it.language +
                (if (it.country.isNotBlank()) "-${it.country}" else "") +
                (if (it.variant.isNotBlank()) "-${it.variant}" else "")

        SUPPORTED_LOCALES.contains(locale)
    }

    ListDialog(
        state = showDialog,
        header = Header.Default(
            title = stringResource(R.string.ui_settings_language_title),
            icon = IconSource(
                painter = IconResource.fromImageVector(Icons.Default.Translate).asPainterResource(),
                contentDescription = null,
            )
        ),
        selection = ListSelection.Single(
            showRadioButtons = true,
            options = IntRange(0, locales.size - 1).map {index ->
                val locale = locales[index]!!

                ListOption(
                    titleText = locale.displayName,
                    subtitleText = locale.getDisplayName(Locale.ENGLISH),
                )
            }.toList(),
            positiveButton = SelectionButton(
                icon = IconSource(
                    painter = IconResource.fromImageVector(Icons.Default.CheckCircle).asPainterResource(),
                    contentDescription = null,
                ),
                text = stringResource(android.R.string.ok),
                type = ButtonStyle.TEXT,
            )
        ) {index, _ ->
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
        title = stringResource(R.string.ui_settings_language_title),
        leading = {
            Icon(
                Icons.Default.Translate,
                contentDescription = null,
            )
        },
    )
}