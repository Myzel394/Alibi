package app.myzel394.alibi.recording

import org.junit.Assert.*
import org.junit.Test

class ShortcutRequestTest {
    @Test fun exportedLauncherRejectsRequestsNotIssuedByOurKeyService() {
        ShortcutRequest.clear()
        assertFalse(ShortcutRequest.matches(null))
        assertFalse(ShortcutRequest.matches("external-request"))
        val token = ShortcutRequest.issue()
        assertTrue(ShortcutRequest.matches(token))
        assertFalse(ShortcutRequest.matches("external-request"))
        ShortcutRequest.clear()
        assertFalse(ShortcutRequest.matches(token))
    }
    @Test fun openingAppAfterBlockedBackgroundLaunchDoesNotStartAStaleRecording() {
        var now = 100L
        val launch = PendingShortcutLaunch { now }
        val token = launch.issue()
        assertTrue(launch.matches(token))
        now += ShortcutRequest.TIMEOUT_MS
        assertFalse("An expired key press must not execute when the user later opens the app", launch.matches(token))
    }
    @Test fun anOldTimeoutDoesNotCancelTheNextPhysicalKeyPress() {
        val launch = PendingShortcutLaunch()
        val old = launch.issue()
        val current = launch.issue()
        assertFalse(launch.cancel(old))
        assertTrue(launch.matches(current))
        assertTrue(launch.cancel(current))
        assertFalse(launch.matches(current))
    }
}
