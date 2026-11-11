package app.myzel394.alibi.recording

/** A key stops its own lens session; the other key must not silently switch or stop it. */
enum class ShortcutCamera { BACK, FRONT }
enum class ShortcutDecision { START, STOP, BUSY }
fun cameraShortcutDecision(requested: ShortcutCamera, active: ShortcutCamera?, busy: Boolean): ShortcutDecision = when {
    active == requested -> ShortcutDecision.STOP
    active != null || busy -> ShortcutDecision.BUSY
    else -> ShortcutDecision.START
}
