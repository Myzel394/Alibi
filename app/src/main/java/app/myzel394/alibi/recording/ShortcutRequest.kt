package app.myzel394.alibi.recording

/** Monotonic time keeps delayed launches independent of wall-clock changes. */
internal class PendingShortcutLaunch(private val clock: () -> Long = { System.nanoTime() / 1_000_000 }) {
    private var token: String? = null
    private var issuedAt = 0L
    fun issue(): String = java.util.UUID.randomUUID().toString().also {
        token = it
        issuedAt = clock()
    }
    fun matches(candidate: String?): Boolean {
        val age = clock() - issuedAt
        return candidate != null && candidate == token && age >= 0 && age < ShortcutRequest.TIMEOUT_MS
    }
    fun clear() { token = null }
    fun cancel(candidate: String): Boolean {
        if (candidate != token) return false
        clear()
        return true
    }
}

/** Exported launcher activities only accept fresh requests issued by our key service. */
object ShortcutRequest {
    const val TIMEOUT_MS = 6000L
    private val pending = PendingShortcutLaunch()
    fun issue() = pending.issue()
    fun matches(candidate: String?) = pending.matches(candidate)
    fun clear() = pending.clear()
    fun cancel(candidate: String) = pending.cancel(candidate)
}
