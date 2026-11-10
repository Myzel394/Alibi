package app.myzel394.alibi.recording

import app.myzel394.alibi.db.AppSettings
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class RecordingSettingsMigrationTest {
    @Test fun existingSettingsRemainReadableAndDoNotEnableGlobalKeysWithoutOptIn() {
        val old = """{"hasSeenOnboarding":true,"maxDuration":900000,"intervalDuration":60000}"""
        val settings = Json.decodeFromString(AppSettings.serializer(), old)
        assertTrue(settings.hasSeenOnboarding)
        assertEquals(900000L, settings.maxDuration)
        assertFalse(settings.hardwareShortcutEnabled)
        assertFalse(settings.recordingIndicatorEnabled)
        assertNull(settings.videoRecorderSettings.backCamera)
    }
    @Test fun optedInControlsSurviveSettingsExportAndImport() {
        val settings = AppSettings(hardwareShortcutEnabled = true, recordingIndicatorEnabled = true)
        val restored = AppSettings.fromExportedString(settings.exportToString())
        assertTrue(restored.hardwareShortcutEnabled)
        assertTrue(restored.recordingIndicatorEnabled)
    }
    @Test fun rearPhysicalCameraPreferenceSurvivesExportAndImport() {
        val selected = app.myzel394.alibi.db.CameraSelection("rear:logical", "tele:physical")
        val settings = AppSettings(videoRecorderSettings = app.myzel394.alibi.db.VideoRecorderSettings(backCamera = selected))
        assertEquals(selected, AppSettings.fromExportedString(settings.exportToString()).videoRecorderSettings.backCamera)
    }
}
