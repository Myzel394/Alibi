package app.myzel394.alibi.ui.enums

sealed class Screen(val route: String) {
    data object AudioRecorder : Screen("audio-recorder")
    data object Settings : Screen("settings")
    data object Welcome : Screen("welcome")
    data object CustomRecordingNotifications : Screen("custom-recording-notifications")

    fun withArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }
}
