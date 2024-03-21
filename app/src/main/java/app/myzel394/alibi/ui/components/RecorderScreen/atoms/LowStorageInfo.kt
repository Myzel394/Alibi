package app.myzel394.alibi.ui.components.RecorderScreen.atoms

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.myzel394.alibi.R
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.helpers.BatchesFolder
import app.myzel394.alibi.helpers.VideoBatchesFolder
import app.myzel394.alibi.ui.components.atoms.MessageBox
import app.myzel394.alibi.ui.components.atoms.MessageType

@Composable
fun LowStorageInfo(
    appSettings: AppSettings,
) {
    val context = LocalContext.current
    val availableBytes =
        VideoBatchesFolder.importFromFolder(appSettings.saveFolder, context).getAvailableBytes()

    val bytesPerMinute = BatchesFolder.requiredBytesForOneMinuteOfRecording(appSettings)
    val requiredBytes = appSettings.maxDuration / 1000 / 60 * bytesPerMinute

    // Allow for a 10% margin of error
    val isLowOnStorage = availableBytes < requiredBytes * 1.1
    println("LowStorageInfo: availableBytes: $availableBytes, requiredBytes: $requiredBytes, isLowOnStorage: $isLowOnStorage")

    if (isLowOnStorage)
        Box(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            MessageBox(
                type = MessageType.WARNING,
                message = stringResource(R.string.ui_recorder_lowOnStorage_hint),
            )
        }
}
