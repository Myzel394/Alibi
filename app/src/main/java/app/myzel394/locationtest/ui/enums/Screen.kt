package app.myzel394.locationtest.ui.enums

sealed class Screen(val route: String) {
    object AudioRecorder : Screen("audio-recorder")
    object Settings : Screen("settings")

    fun withArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }
}
