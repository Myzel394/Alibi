package app.myzel394.alibi.ui.components.SettingsScreen.Tiles

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Merge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import app.myzel394.alibi.R
import app.myzel394.alibi.dataStore
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.ui.components.atoms.Settings.ListTile
import app.myzel394.alibi.ui.components.atoms.Settings.ListTileSwitch
import kotlinx.coroutines.launch

@Composable
fun CombineBatchesTile(
    settings: AppSettings,
    icon: ImageVector = Icons.Outlined.Merge,
    title: String = stringResource(R.string.ui_settings_combineBatches_title),
    description: String? = stringResource(R.string.ui_settings_combineBatches_description),
) {
    val scope = rememberCoroutineScope()
    val dataStore = LocalContext.current.dataStore

    ListTile(
        icon = icon,
        title = title,
        description = description,
    ) {
        ListTileSwitch(
            checked = settings.combine_batches,
            onCheckedChange = {
                scope.launch {
                    dataStore.updateData { currentSettings ->
                        currentSettings.setCombineBatches(it)
                    }
                }
            },
        )
    }
}
