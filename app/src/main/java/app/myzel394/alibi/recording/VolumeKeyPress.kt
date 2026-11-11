package app.myzel394.alibi.recording

/** One toggle per physical hold; release reports whether normal volume adjustment is due. */
class VolumeKeyPress {
    var pressed = false
        private set
    private var triggered = false
    fun down(repeat: Int): Boolean {
        if (pressed || repeat != 0) return false
        pressed = true
        triggered = false
        return true
    }
    fun timeout(): Boolean {
        if (!pressed || triggered) return false
        triggered = true
        return true
    }
    fun up(cancelled: Boolean): Boolean {
        val shortPress = pressed && !triggered && !cancelled
        reset()
        return shortPress
    }
    fun reset() { pressed = false; triggered = false }
}
