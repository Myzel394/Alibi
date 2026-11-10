package app.myzel394.alibi.recording

import org.junit.Assert.*
import org.junit.Test

class VolumeKeyPressTest {
    @Test fun shortPressAdjustsVolumeWithoutToggle() {
        val press = VolumeKeyPress()
        assertTrue(press.down(0))
        assertTrue(press.up(false))
        assertFalse(press.timeout())
    }
    @Test fun holdTogglesOnceDespiteRepeatsAndDoesNotAdjustVolume() {
        val press = VolumeKeyPress()
        assertTrue(press.down(0))
        assertFalse(press.down(1))
        assertTrue(press.timeout())
        assertFalse(press.timeout())
        assertFalse(press.up(false))
    }
    @Test fun secondPhysicalHoldCanToggleAgain() {
        val press = VolumeKeyPress()
        press.down(0); press.timeout(); press.up(false)
        assertTrue(press.down(0)); assertTrue(press.timeout())
    }
    @Test fun cancelledAndInterruptedKeysHaveNoVolumeOrRecordingSideEffect() {
        val press = VolumeKeyPress()
        press.down(0); assertFalse(press.up(true)); assertFalse(press.timeout())
        press.down(0); press.reset(); assertFalse(press.timeout()); assertFalse(press.up(false))
    }
    @Test fun orphanedRepeatOrReleaseIsIgnored() {
        val press = VolumeKeyPress()
        assertFalse(press.down(2)); assertFalse(press.timeout()); assertFalse(press.up(false))
    }
}
