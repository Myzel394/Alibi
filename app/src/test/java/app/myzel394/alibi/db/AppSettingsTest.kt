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
        // Test with combine_batches = true (existing test)
        var originalSettingsTrue = AppSettings().setCombineBatches(true)
        // Add scheduler settings to the existing serialization test
        originalSettingsTrue = originalSettingsTrue
            .setSchedulerEnabled(true)
            .setSchedulerTime(10, 30)
            .setSchedulerDays(setOf(1, 3, 5))

        val serializedSettingsTrue = originalSettingsTrue.exportToString()
        val deserializedSettingsTrue = AppSettings.fromExportedString(serializedSettingsTrue)
        assertTrue("Deserialized combine_batches should be true", deserializedSettingsTrue.combine_batches)
        assertTrue("Deserialized schedulerEnabled should be true", deserializedSettingsTrue.schedulerEnabled)
        assertEquals("Deserialized schedulerHour should be 10", 10, deserializedSettingsTrue.schedulerHour)
        assertEquals("Deserialized schedulerMinute should be 30", 30, deserializedSettingsTrue.schedulerMinute)
        assertEquals("Deserialized schedulerDays should be {1, 3, 5}", setOf(1, 3, 5), deserializedSettingsTrue.schedulerDays)


        // Test with combine_batches = false (existing test)
        var originalSettingsFalse = AppSettings().setCombineBatches(false)
        // Add different scheduler settings for this case
        originalSettingsFalse = originalSettingsFalse
            .setSchedulerEnabled(false)
            .setSchedulerTime(20, 0)
            .setSchedulerDays(setOf(2, 4, 6))

        val serializedSettingsFalse = originalSettingsFalse.exportToString()
        val deserializedSettingsFalse = AppSettings.fromExportedString(serializedSettingsFalse)
        assertFalse("Deserialized combine_batches should be false", deserializedSettingsFalse.combine_batches)
        assertFalse("Deserialized schedulerEnabled should be false", deserializedSettingsFalse.schedulerEnabled)
        assertEquals("Deserialized schedulerHour should be 20", 20, deserializedSettingsFalse.schedulerHour)
        assertEquals("Deserialized schedulerMinute should be 0", 0, deserializedSettingsFalse.schedulerMinute)
        assertEquals("Deserialized schedulerDays should be {2, 4, 6}", setOf(2, 4, 6), deserializedSettingsFalse.schedulerDays)
    }

    @Test
    fun testSchedulerDefaults() {
        val settings = AppSettings()
        assertFalse("Default schedulerEnabled should be false", settings.schedulerEnabled)
        assertEquals("Default schedulerHour should be 9", 9, settings.schedulerHour)
        assertEquals("Default schedulerMinute should be 0", 0, settings.schedulerMinute)
        assertTrue("Default schedulerDays should be empty", settings.schedulerDays.isEmpty())
    }

    @Test
    fun testSetSchedulerEnabled() {
        var settings = AppSettings()
        settings = settings.setSchedulerEnabled(true)
        assertTrue("schedulerEnabled should be true after setting to true", settings.schedulerEnabled)
        settings = settings.setSchedulerEnabled(false)
        assertFalse("schedulerEnabled should be false after setting to false", settings.schedulerEnabled)
    }

    @Test
    fun testSetSchedulerTime() {
        var settings = AppSettings()
        settings = settings.setSchedulerTime(14, 35)
        assertEquals("schedulerHour should be 14 after setting", 14, settings.schedulerHour)
        assertEquals("schedulerMinute should be 35 after setting", 35, settings.schedulerMinute)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSetSchedulerTimeInvalidHourLow() {
        AppSettings().setSchedulerTime(-1, 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSetSchedulerTimeInvalidHourHigh() {
        AppSettings().setSchedulerTime(24, 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSetSchedulerTimeInvalidMinuteLow() {
        AppSettings().setSchedulerTime(0, -1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSetSchedulerTimeInvalidMinuteHigh() {
        AppSettings().setSchedulerTime(0, 60)
    }

    @Test
    fun testSetSchedulerDays() {
        var settings = AppSettings()
        val days = setOf(1, 2, 3) // Sunday, Monday, Tuesday
        settings = settings.setSchedulerDays(days)
        assertEquals("schedulerDays should be {1, 2, 3} after setting", days, settings.schedulerDays)

        val emptyDays = emptySet<Int>()
        settings = settings.setSchedulerDays(emptyDays)
        assertTrue("schedulerDays should be empty after setting to empty", settings.schedulerDays.isEmpty())
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSetSchedulerDaysInvalidDayLow() {
        AppSettings().setSchedulerDays(setOf(0, 1))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSetSchedulerDaysInvalidDayHigh() {
        AppSettings().setSchedulerDays(setOf(7, 8))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testSetSchedulerDaysInvalidDayMix() {
        AppSettings().setSchedulerDays(setOf(1, 8))
    }
}
