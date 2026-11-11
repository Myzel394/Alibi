package app.myzel394.alibi.recording

import org.junit.Assert.*
import org.junit.Test

class CameraShortcutPolicyTest {
    @Test fun eitherLensCanStartWhenIdle() {
        for (lens in ShortcutCamera.entries) assertEquals(ShortcutDecision.START, cameraShortcutDecision(lens, null, false))
    }
    @Test fun eachKeyStopsItsOwnLensEvenThoughTheRecorderIsBusy() {
        for (lens in ShortcutCamera.entries) assertEquals(ShortcutDecision.STOP, cameraShortcutDecision(lens, lens, true))
    }
    @Test fun otherKeyCannotStopOrSwitchAnActiveCamera() {
        assertEquals(ShortcutDecision.BUSY, cameraShortcutDecision(ShortcutCamera.FRONT, ShortcutCamera.BACK, true))
        assertEquals(ShortcutDecision.BUSY, cameraShortcutDecision(ShortcutCamera.BACK, ShortcutCamera.FRONT, true))
    }
    @Test fun audioRecordingOrSavingBlocksBothVideoShortcuts() {
        for (lens in ShortcutCamera.entries) assertEquals(ShortcutDecision.BUSY, cameraShortcutDecision(lens, null, true))
    }
}
