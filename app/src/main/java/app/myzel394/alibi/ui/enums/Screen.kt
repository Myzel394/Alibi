package app.myzel394.alibi.ui.enums

import kotlinx.serialization.Serializable

sealed class Screen {
    @Serializable
    data object Recorder
    @Serializable
    data object Settings
    @Serializable
    data object Welcome
    @Serializable
    object CustomRecordingNotifications
    @Serializable
    object About
}
