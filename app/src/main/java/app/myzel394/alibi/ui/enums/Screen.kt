package app.myzel394.alibi.ui.enums

sealed class Screen(val route: String) {
    object Recorder : Screen("recorder")
    object Settings : Screen("settings")
    object Welcome : Screen("welcome")

    fun withArgs(vararg args: String): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }
}
