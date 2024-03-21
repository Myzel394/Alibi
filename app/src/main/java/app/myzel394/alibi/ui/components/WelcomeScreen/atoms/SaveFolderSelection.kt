package app.myzel394.alibi.ui.components.WelcomeScreen.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import app.myzel394.alibi.R
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.ui.RECORDER_MEDIA_SELECTED_VALUE
import app.myzel394.alibi.ui.components.atoms.MessageBox
import app.myzel394.alibi.ui.components.atoms.MessageType

const val CUSTOM_FOLDER = "custom"

@Composable
fun SaveFolderSelection(
    modifier: Modifier = Modifier,
    appSettings: AppSettings,
    saveFolder: String?,
    isLowOnStorage: Boolean,
    onSaveFolderChange: (String?) -> Unit,
) {
    val OPTIONS = mapOf<String?, Pair<String, ImageVector>>(
        null to (stringResource(R.string.ui_welcome_saveFolder_values_internal) to Icons.Default.Lock),
        RECORDER_MEDIA_SELECTED_VALUE to (stringResource(R.string.ui_welcome_saveFolder_values_media) to Icons.Default.PermMedia),
        CUSTOM_FOLDER to (stringResource(R.string.ui_welcome_saveFolder_values_custom) to Icons.Default.Folder),
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .then(modifier),
            verticalArrangement = Arrangement.Center,
        ) {
            for ((folder, pair) in OPTIONS) {
                val (label, icon) = pair
                val a11yLabel = stringResource(
                    R.string.a11y_selectValue,
                    label
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .semantics {
                            contentDescription = a11yLabel
                        }
                        .clickable {
                            onSaveFolderChange(folder)
                        }
                        .padding(16.dp)
                        .padding(end = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        RadioButton(
                            selected = saveFolder == folder,
                            onClick = { onSaveFolderChange(folder) },
                        )
                        Text(label)
                    }
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier
                            .size(ButtonDefaults.IconSize)
                    )
                }
            }
        }
        if (isLowOnStorage)
            MessageBox(
                type = MessageType.ERROR,
                message = stringResource(R.string.ui_welcome_saveFolder_externalRequired)
            )
    }
}