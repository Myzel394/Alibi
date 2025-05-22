package app.myzel394.alibi.db

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class AppSettingsTest {

    @Test
    fun testCombineBatchesDefault() {
        val settings = AppSettings()
        assertTrue("Default value of combine_batches should be true", settings.combine_batches)
    }

    @Test
    fun testSetCombineBatches() {
        var settings = AppSettings()
        settings = settings.setCombineBatches(false)
        assertFalse("combine_batches should be false after setting to false", settings.combine_batches)

        settings = settings.setCombineBatches(true)
        assertTrue("combine_batches should be true after setting to true", settings.combine_batches)
    }

    @Test
    fun testSerializationDeserialization() {
        // Test with combine_batches = true
        val originalSettingsTrue = AppSettings().setCombineBatches(true)
        val serializedSettingsTrue = originalSettingsTrue.exportToString()
        val deserializedSettingsTrue = AppSettings.fromExportedString(serializedSettingsTrue)
        assertTrue("Deserialized combine_batches should be true", deserializedSettingsTrue.combine_batches)

        // Test with combine_batches = false
        val originalSettingsFalse = AppSettings().setCombineBatches(false)
        val serializedSettingsFalse = originalSettingsFalse.exportToString()
        val deserializedSettingsFalse = AppSettings.fromExportedString(serializedSettingsFalse)
        assertFalse("Deserialized combine_batches should be false", deserializedSettingsFalse.combine_batches)
    }
}
