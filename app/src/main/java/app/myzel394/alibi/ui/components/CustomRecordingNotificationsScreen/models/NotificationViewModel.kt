package app.myzel394.alibi.ui.components.CustomRecordingNotificationsScreen.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import app.myzel394.alibi.R
import app.myzel394.alibi.db.NotificationSettings

class NotificationViewModel : ViewModel() {
    // We want to show the actual translated strings of the preset
    // in the preview but don't want to save them to the database
    // because they should be retrieved in the notification itself.
    // Thus we save whether the preset has been changed by the user
    private var _presetChanged = false

    private var _title = mutableStateOf("")
    val title: String
        get() = _title.value
    private var _description = mutableStateOf("")
    val description: String
        get() = _description.value

    var showOngoing: Boolean by mutableStateOf(true)
    var icon: Int by mutableIntStateOf(R.drawable.launcher_monochrome_noopacity)

    // `preset` can't be used as a variable name here because
    // the compiler throws a strange error then
    var notificationPreset: NotificationSettings.Preset? by mutableStateOf(null)

    private var _hasBeenInitialized = false;


    fun setPreset(title: String, description: String, preset: NotificationSettings.Preset) {
        _presetChanged = false

        _title.value = title
        _description.value = description
        showOngoing = preset.showOngoing
        icon = preset.iconID
        this.notificationPreset = preset
    }

    fun setTitle(title: String) {
        _presetChanged = true
        _title.value = title
    }

    fun setDescription(description: String) {
        _presetChanged = true
        _description.value = description
    }

    fun initialize(
        title: String,
        description: String,
    ) {
        if (_hasBeenInitialized) {
            return
        }

        _title.value = title
        _description.value = description
        _hasBeenInitialized = true
    }
}
