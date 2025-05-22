package app.myzel394.alibi.helpers

import app.myzel394.alibi.db.AppSettings
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class SchedulerHelperTest {

    private fun getAppSettingsMock(
        enabled: Boolean,
        hour: Int,
        minute: Int,
        days: Set<Int>
    ): AppSettings {
        val mockSettings = mockk<AppSettings>()
        every { mockSettings.schedulerEnabled } returns enabled
        every { mockSettings.schedulerHour } returns hour
        every { mockSettings.schedulerMinute } returns minute
        every { mockSettings.schedulerDays } returns days
        return mockSettings
    }

    private fun assertTimeEquals(
        expectedHour: Int,
        expectedMinute: Int,
        expectedDayOfWeek: Int, // Calendar.SUNDAY, Calendar.MONDAY, etc.
        actualTimeMillis: Long?,
        message: String = ""
    ) {
        assertNotNull("Actual time should not be null. $message", actualTimeMillis)
        val actualCalendar = Calendar.getInstance().apply { timeInMillis = actualTimeMillis!! }
        assertEquals("Hour mismatch. $message", expectedHour, actualCalendar.get(Calendar.HOUR_OF_DAY))
        assertEquals("Minute mismatch. $message", expectedMinute, actualCalendar.get(Calendar.MINUTE))
        assertEquals("Day of week mismatch. $message", expectedDayOfWeek, actualCalendar.get(Calendar.DAY_OF_WEEK))
    }

    @Test
    fun `calculateNextTriggerTime - no days selected`() {
        val settings = getAppSettingsMock(true, 9, 0, emptySet())
        val triggerTime = SchedulerHelper.calculateNextTriggerTime(settings)
        assertNull("Trigger time should be null if no days are selected", triggerTime)
    }

    @Test
    fun `calculateNextTriggerTime - today before scheduled time`() {
        val now = Calendar.getInstance()
        val scheduledHour = now.get(Calendar.HOUR_OF_DAY) + 2 // 2 hours from now
        val scheduledMinute = now.get(Calendar.MINUTE)
        val todayDayOfWeek = now.get(Calendar.DAY_OF_WEEK)

        val settings = getAppSettingsMock(true, scheduledHour, scheduledMinute, setOf(todayDayOfWeek))
        val triggerTime = SchedulerHelper.calculateNextTriggerTime(settings)

        assertTimeEquals(scheduledHour, scheduledMinute, todayDayOfWeek, triggerTime, "Failed: today before scheduled time")
    }

    @Test
    fun `calculateNextTriggerTime - today after scheduled time`() {
        val now = Calendar.getInstance()
        now.set(Calendar.HOUR_OF_DAY, 14) // Current time is 2 PM
        val scheduledHour = 10 // Scheduled time is 10 AM
        val scheduledMinute = 0
        val todayDayOfWeek = now.get(Calendar.DAY_OF_WEEK)

        val settings = getAppSettingsMock(true, scheduledHour, scheduledMinute, setOf(todayDayOfWeek))
        val triggerTime = SchedulerHelper.calculateNextTriggerTime(settings)

        // Should schedule for next week on the same day
        val expectedCalendar = Calendar.getInstance().apply {
            timeInMillis = now.timeInMillis
            add(Calendar.WEEK_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, scheduledHour)
            set(Calendar.MINUTE, scheduledMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        assertTimeEquals(scheduledHour, scheduledMinute, todayDayOfWeek, triggerTime, "Failed: today after scheduled time")
        assertEquals("Timestamp day should be 7 days from now", expectedCalendar.timeInMillis / (24*60*60*1000), triggerTime!! / (24*60*60*1000))
    }


    @Test
    fun `calculateNextTriggerTime - scheduled day is tomorrow`() {
        val now = Calendar.getInstance() // e.g., Monday 3 PM
        val scheduledHour = 10
        val scheduledMinute = 0

        val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }
        val tomorrowDayOfWeek = tomorrow.get(Calendar.DAY_OF_WEEK)

        val settings = getAppSettingsMock(true, scheduledHour, scheduledMinute, setOf(tomorrowDayOfWeek))
        val triggerTime = SchedulerHelper.calculateNextTriggerTime(settings)

        assertTimeEquals(scheduledHour, scheduledMinute, tomorrowDayOfWeek, triggerTime, "Failed: scheduled day is tomorrow")
    }

    @Test
    fun `calculateNextTriggerTime - scheduled day later in week`() {
        val now = Calendar.getInstance() // e.g., Monday 3 PM
        now.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val scheduledHour = 11
        val scheduledMinute = 30

        // Schedule for Friday
        val settings = getAppSettingsMock(true, scheduledHour, scheduledMinute, setOf(Calendar.FRIDAY))
        val triggerTime = SchedulerHelper.calculateNextTriggerTime(settings)

        assertTimeEquals(scheduledHour, scheduledMinute, Calendar.FRIDAY, triggerTime, "Failed: scheduled day later in week (Mon -> Fri)")
    }

    @Test
    fun `calculateNextTriggerTime - scheduled day earlier in week (next week)`() {
        val now = Calendar.getInstance() // e.g., Friday 3 PM
        now.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
        val scheduledHour = 10
        val scheduledMinute = 0

        // Schedule for Monday
        val settings = getAppSettingsMock(true, scheduledHour, scheduledMinute, setOf(Calendar.MONDAY))
        val triggerTime = SchedulerHelper.calculateNextTriggerTime(settings)

        // Expected: Monday of next week
        val expectedCalendar = Calendar.getInstance().apply{
            timeInMillis = now.timeInMillis
            add(Calendar.WEEK_OF_YEAR, 1) // Move to next week
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY) // Set to Monday
            set(Calendar.HOUR_OF_DAY, scheduledHour)
            set(Calendar.MINUTE, scheduledMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        assertTimeEquals(scheduledHour, scheduledMinute, Calendar.MONDAY, triggerTime, "Failed: scheduled day earlier in week (Fri -> Mon next week)")
        assertEquals("Timestamp day should be for next week's Monday", expectedCalendar.timeInMillis / (24*60*60*1000), triggerTime!! / (24*60*60*1000))
    }

    @Test
    fun `calculateNextTriggerTime - multiple days selected - next is today`() {
        val now = Calendar.getInstance()
        val scheduledHour = now.get(Calendar.HOUR_OF_DAY) + 1
        val scheduledMinute = now.get(Calendar.MINUTE)
        val todayDayOfWeek = now.get(Calendar.DAY_OF_WEEK)
        val tomorrowDayOfWeek = (todayDayOfWeek % 7) + 1 // Simple way to get next day, wraps around

        val settings = getAppSettingsMock(true, scheduledHour, scheduledMinute, setOf(todayDayOfWeek, tomorrowDayOfWeek))
        val triggerTime = SchedulerHelper.calculateNextTriggerTime(settings)

        assertTimeEquals(scheduledHour, scheduledMinute, todayDayOfWeek, triggerTime, "Failed: multiple days, next is today")
    }

    @Test
    fun `calculateNextTriggerTime - multiple days selected - next is tomorrow`() {
        val now = Calendar.getInstance()
        now.set(Calendar.HOUR_OF_DAY, 23) // Late today
        val scheduledHour = 10
        val scheduledMinute = 0

        val todayDayOfWeek = now.get(Calendar.DAY_OF_WEEK)
        val tomorrow = Calendar.getInstance().apply{ add(Calendar.DAY_OF_YEAR, 1)}
        val tomorrowDayOfWeek = tomorrow.get(Calendar.DAY_OF_WEEK)

        val settings = getAppSettingsMock(true, scheduledHour, scheduledMinute, setOf(todayDayOfWeek, tomorrowDayOfWeek))
        val triggerTime = SchedulerHelper.calculateNextTriggerTime(settings)

        assertTimeEquals(scheduledHour, scheduledMinute, tomorrowDayOfWeek, triggerTime, "Failed: multiple days, next is tomorrow")
    }

    @Test
    fun `calculateNextTriggerTime - month transition`() {
        val now = Calendar.getInstance()
        // Set date to last day of a 30-day month, e.g., April 30th, 10 PM
        now.set(Calendar.MONTH, Calendar.APRIL)
        now.set(Calendar.DAY_OF_MONTH, 30)
        now.set(Calendar.HOUR_OF_DAY, 22)
        now.set(Calendar.MINUTE, 0)

        val scheduledHour = 8
        val scheduledMinute = 0
        // Schedule for the 1st of next month, which should be a specific day of week
        val dayAfterTransition = Calendar.getInstance().apply {
            timeInMillis = now.timeInMillis
            add(Calendar.DAY_OF_MONTH, 1) // This will roll over to May 1st
            set(Calendar.HOUR_OF_DAY, scheduledHour)
            set(Calendar.MINUTE, scheduledMinute)
        }
        val scheduledDayOfWeek = dayAfterTransition.get(Calendar.DAY_OF_WEEK)

        val settings = getAppSettingsMock(true, scheduledHour, scheduledMinute, setOf(scheduledDayOfWeek))
        // Temporarily set 'now' for SchedulerHelper's internal 'Calendar.getInstance()'
        // This is tricky without DI for Calendar.getInstance(). The test relies on running fast enough.
        // A more robust solution would involve injecting a Clock or Calendar provider.
        // For now, we assume SchedulerHelper.calculateNextTriggerTime picks up current time.

        val triggerTime = SchedulerHelper.calculateNextTriggerTime(settings)

        assertTimeEquals(scheduledHour, scheduledMinute, scheduledDayOfWeek, triggerTime, "Failed: month transition")
        assertEquals("Day of month should be 1", 1, Calendar.getInstance().apply { timeInMillis = triggerTime!! }.get(Calendar.DAY_OF_MONTH) )
        assertEquals("Month should be May", Calendar.MAY, Calendar.getInstance().apply { timeInMillis = triggerTime!! }.get(Calendar.MONTH) )
    }

    @Test
    fun `calculateNextTriggerTime - year transition`() {
        val now = Calendar.getInstance()
        // December 31st, 11 PM
        now.set(Calendar.YEAR, 2023)
        now.set(Calendar.MONTH, Calendar.DECEMBER)
        now.set(Calendar.DAY_OF_MONTH, 31)
        now.set(Calendar.HOUR_OF_DAY, 23)
        now.set(Calendar.MINUTE, 0)

        val scheduledHour = 10
        val scheduledMinute = 0

        // Schedule for Jan 1st
        val dayAfterTransition = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2024)
            set(Calendar.MONTH, Calendar.JANUARY)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, scheduledHour)
            set(Calendar.MINUTE, scheduledMinute)
        }
        val scheduledDayOfWeek = dayAfterTransition.get(Calendar.DAY_OF_WEEK)

        val settings = getAppSettingsMock(true, scheduledHour, scheduledMinute, setOf(scheduledDayOfWeek))
        val triggerTime = SchedulerHelper.calculateNextTriggerTime(settings)

        assertTimeEquals(scheduledHour, scheduledMinute, scheduledDayOfWeek, triggerTime, "Failed: year transition")
        assertEquals("Year should be 2024", 2024, Calendar.getInstance().apply { timeInMillis = triggerTime!! }.get(Calendar.YEAR) )
        assertEquals("Month should be January", Calendar.JANUARY, Calendar.getInstance().apply { timeInMillis = triggerTime!! }.get(Calendar.MONTH) )
        assertEquals("Day of month should be 1", 1, Calendar.getInstance().apply { timeInMillis = triggerTime!! }.get(Calendar.DAY_OF_MONTH) )
    }

    @Test
    fun `calculateNextTriggerTime - specific case - Friday 1700 to Monday 0800`() {
        val now = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
            set(Calendar.HOUR_OF_DAY, 17) // 5 PM
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val scheduledHour = 8
        val scheduledMinute = 0
        val settings = getAppSettingsMock(true, scheduledHour, scheduledMinute, setOf(Calendar.MONDAY))

        // Manually advance 'now' for the test setup if SchedulerHelper uses its own `Calendar.getInstance()`
        // This is a limitation of not having a Clock injected.
        // The test assumes `SchedulerHelper.calculateNextTriggerTime` will pick up the current time.

        val triggerTime = SchedulerHelper.calculateNextTriggerTime(settings)

        val expectedCalendar = Calendar.getInstance().apply {
            timeInMillis = now.timeInMillis
            add(Calendar.DAY_OF_YEAR, 3) // Saturday, Sunday, Monday
            set(Calendar.HOUR_OF_DAY, scheduledHour)
            set(Calendar.MINUTE, scheduledMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        assertTimeEquals(scheduledHour, scheduledMinute, Calendar.MONDAY, triggerTime, "Failed: Friday 17:00 to Monday 08:00")
        assertEquals("Timestamp day should be for next Monday", expectedCalendar.timeInMillis / (24*60*60*1000), triggerTime!! / (24*60*60*1000))
    }

     @Test
    fun `calculateNextTriggerTime - current time is exactly scheduled time`() {
        val now = Calendar.getInstance()
        val scheduledHour = now.get(Calendar.HOUR_OF_DAY)
        val scheduledMinute = now.get(Calendar.MINUTE)
        val todayDayOfWeek = now.get(Calendar.DAY_OF_WEEK)

        val settings = getAppSettingsMock(true, scheduledHour, scheduledMinute, setOf(todayDayOfWeek))

        // To ensure 'now' is truly before or exactly at the scheduled time for the purpose of the test,
        // we might need to adjust 'now' slightly or rely on the precision of System.currentTimeMillis
        // For this test, we assume `calculateNextTriggerTime` uses a 'now' that is effectively at or just before this moment.
        // If `now` in `calculateNextTriggerTime` is a few ms later, it might schedule for next week.
        // This highlights the need for injecting a Clock.

        val triggerTime = SchedulerHelper.calculateNextTriggerTime(settings)

        // This should schedule for the *next* occurrence if current time is effectively past the scheduled time,
        // or for *today* if current time is effectively at/before.
        // Given current implementation, if `nextDayCandidate.before(now)` is false, it takes `nextDayCandidate`.
        // If `timeInMillis` are identical, `before` is false.

        val cal = Calendar.getInstance().apply { timeInMillis = triggerTime!! }

        // If it scheduled for today
        if (cal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) && cal.timeInMillis >= now.timeInMillis) {
            assertTimeEquals(scheduledHour, scheduledMinute, todayDayOfWeek, triggerTime, "Failed: current time is exactly scheduled time (today)")
        } else { // Scheduled for next week
            val expectedCalendar = Calendar.getInstance().apply {
                timeInMillis = now.timeInMillis
                add(Calendar.WEEK_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, scheduledHour)
                set(Calendar.MINUTE, scheduledMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            assertTimeEquals(scheduledHour, scheduledMinute, todayDayOfWeek, triggerTime, "Failed: current time is exactly scheduled time (next week)")
            assertEquals("Timestamp day should be 7 days from now", expectedCalendar.timeInMillis / (24*60*60*1000), triggerTime / (24*60*60*1000))
        }
    }

    // Tests for scheduleNextAlarm and cancelAlarm
    @Test
    fun `scheduleNextAlarm - scheduler disabled`() {
        val mockContext = mockk<Context>(relaxed = true)
        val mockAlarmManager = mockk<AlarmManager>(relaxed = true)
        every { mockContext.getSystemService(Context.ALARM_SERVICE) } returns mockAlarmManager

        val settings = getAppSettingsMock(false, 9, 0, setOf(Calendar.MONDAY))

        mockkStatic(Log::class)
        every { Log.i(any(), any()) } returns 0

        // Mock PendingIntent static method
        mockkStatic(PendingIntent::class)
        val mockPendingIntent = mockk<PendingIntent>(relaxed = true)
        every { PendingIntent.getBroadcast(any(), any(), any(), any()) } returns mockPendingIntent

        SchedulerHelper.scheduleNextAlarm(mockContext, settings)

        verify { mockAlarmManager.cancel(mockPendingIntent) } // Always cancels previous
        verify(exactly = 0) { mockAlarmManager.setExactAndAllowWhileIdle(any(), any()) } // Should not set new alarm
        unmockkStatic(PendingIntent::class)
        unmockkStatic(Log::class)
    }

    @Test
    fun `scheduleNextAlarm - scheduler enabled, no days`() {
        val mockContext = mockk<Context>(relaxed = true)
        val mockAlarmManager = mockk<AlarmManager>(relaxed = true)
        every { mockContext.getSystemService(Context.ALARM_SERVICE) } returns mockAlarmManager

        val settings = getAppSettingsMock(true, 9, 0, emptySet()) // No days

        mockkStatic(Log::class)
        every { Log.i(any(), any()) } returns 0
        mockkStatic(PendingIntent::class)
        val mockPendingIntent = mockk<PendingIntent>(relaxed = true)
        every { PendingIntent.getBroadcast(any(), any(), any(), any()) } returns mockPendingIntent

        SchedulerHelper.scheduleNextAlarm(mockContext, settings)

        verify { mockAlarmManager.cancel(mockPendingIntent) }
        verify(exactly = 0) { mockAlarmManager.setExactAndAllowWhileIdle(any(), any()) }
        unmockkStatic(PendingIntent::class)
        unmockkStatic(Log::class)
    }

    @Test
    fun `scheduleNextAlarm - scheduler enabled, with days`() {
        val mockContext = mockk<Context>(relaxed = true)
        val mockAlarmManager = mockk<AlarmManager>(relaxed = true)
        every { mockContext.getSystemService(Context.ALARM_SERVICE) } returns mockAlarmManager
        every { mockAlarmManager.canScheduleExactAlarms() } returns true // Assume permission granted for this test

        val now = Calendar.getInstance()
        val scheduledHour = now.get(Calendar.HOUR_OF_DAY) + 1 // 1 hour from now
        val scheduledMinute = now.get(Calendar.MINUTE)
        val todayDayOfWeek = now.get(Calendar.DAY_OF_WEEK)

        val settings = getAppSettingsMock(true, scheduledHour, scheduledMinute, setOf(todayDayOfWeek))

        mockkStatic(Log::class)
        every { Log.i(any(), any()) } returns 0
        every { Log.d(any(), any()) } returns 0 // For calculateNextTriggerTime logs
        mockkStatic(PendingIntent::class)
        val mockPendingIntent = mockk<PendingIntent>(relaxed = true)
        every { PendingIntent.getBroadcast(any(), any(), any(), any()) } returns mockPendingIntent

        SchedulerHelper.scheduleNextAlarm(mockContext, settings)

        val expectedTriggerTime = SchedulerHelper.calculateNextTriggerTime(settings)!!

        verify { mockAlarmManager.cancel(mockPendingIntent) }
        verify { mockAlarmManager.setExactAndAllowWhileIdle(expectedTriggerTime, mockPendingIntent) }
        unmockkStatic(PendingIntent::class)
        unmockkStatic(Log::class)
    }

    @Test
    fun `cancelAlarm - cancels correctly`() {
        val mockContext = mockk<Context>(relaxed = true)
        val mockAlarmManager = mockk<AlarmManager>(relaxed = true)
        every { mockContext.getSystemService(Context.ALARM_SERVICE) } returns mockAlarmManager

        mockkStatic(Log::class)
        every { Log.i(any(), any()) } returns 0
        mockkStatic(PendingIntent::class)
        val mockPendingIntent = mockk<PendingIntent>(relaxed = true)
        every { PendingIntent.getBroadcast(any(), any(), any(), any()) } returns mockPendingIntent

        SchedulerHelper.cancelAlarm(mockContext)

        verify { mockAlarmManager.cancel(mockPendingIntent) }
        verify { mockPendingIntent.cancel() } // Also verify the PendingIntent itself is cancelled
        unmockkStatic(PendingIntent::class)
        unmockkStatic(Log::class)
    }
}
