package app.myzel394.alibi.receivers

import android.content.Context
import android.content.Intent
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.helpers.SchedulerHelper
import app.myzel394.alibi.services.AudioRecorderService
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class ScheduledRecordingReceiverTest {

    private lateinit var mockContext: Context
    private lateinit var mockIntent: Intent
    private lateinit var mockAppSettings: AppSettings

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0

        mockContext = mockk(relaxed = true)
        mockIntent = mockk(relaxed = true)
        mockAppSettings = mockk(relaxed = true)

        // Mock DataStore behavior
        val mockDataStore = mockk<androidx.datastore.core.DataStore<AppSettings>>(relaxed = true)
        every { mockContext.dataStore } returns mockDataStore
        every { mockDataStore.data } returns flowOf(mockAppSettings)

        mockkObject(SchedulerHelper) // Mock SchedulerHelper
        every { SchedulerHelper.scheduleNextAlarm(any(), any()) } just runs

        // Mock the companion object/static property of AudioRecorderService
        mockkObject(AudioRecorderService.Companion)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `onReceive - scheduler disabled - no action`() = runTest {
        every { mockIntent.action } returns SchedulerHelper.ACTION_SCHEDULED_RECORDING_START
        every { mockAppSettings.schedulerEnabled } returns false

        val receiver = ScheduledRecordingReceiver()
        receiver.onReceive(mockContext, mockIntent)

        verify(exactly = 0) { mockContext.startForegroundService(any()) }
        verify(exactly = 0) { SchedulerHelper.scheduleNextAlarm(any(), any()) } // Should not reschedule if disabled
    }

    @Test
    fun `onReceive - scheduler enabled - service not active - starts service and reschedules`() = runTest {
        every { mockIntent.action } returns SchedulerHelper.ACTION_SCHEDULED_RECORDING_START
        every { mockAppSettings.schedulerEnabled } returns true
        every { AudioRecorderService.isServiceActive } returns false // Service not active

        val serviceIntentSlot = slot<Intent>()
        every { mockContext.startForegroundService(capture(serviceIntentSlot)) } returns mockk()

        val receiver = ScheduledRecordingReceiver()
        receiver.onReceive(mockContext, mockIntent)

        verify(exactly = 1) { mockContext.startForegroundService(any()) }
        assertEquals(AudioRecorderService::class.java.name, serviceIntentSlot.captured.component?.className)
        assertEquals("init", serviceIntentSlot.captured.action)
        assertEquals(true, serviceIntentSlot.captured.getBooleanExtra("isScheduledStart", false))

        verify(exactly = 1) { SchedulerHelper.scheduleNextAlarm(mockContext, mockAppSettings) }
    }

    @Test
    fun `onReceive - scheduler enabled - service active - skips start, but reschedules`() = runTest {
        every { mockIntent.action } returns SchedulerHelper.ACTION_SCHEDULED_RECORDING_START
        every { mockAppSettings.schedulerEnabled } returns true
        every { AudioRecorderService.isServiceActive } returns true // Service IS active

        val receiver = ScheduledRecordingReceiver()
        receiver.onReceive(mockContext, mockIntent)

        verify(exactly = 0) { mockContext.startForegroundService(any()) } // Should not start service
        verify(exactly = 1) { SchedulerHelper.scheduleNextAlarm(mockContext, mockAppSettings) } // Should still reschedule
    }

    @Test
    fun `onReceive - wrong action - no action`() = runTest {
        every { mockIntent.action } returns "SOME_OTHER_ACTION"
        // AppSettings can be anything here, it shouldn't be reached
        every { mockAppSettings.schedulerEnabled } returns true
        every { AudioRecorderService.isServiceActive } returns false

        val receiver = ScheduledRecordingReceiver()
        receiver.onReceive(mockContext, mockIntent)

        verify(exactly = 0) { mockContext.startForegroundService(any()) }
        verify(exactly = 0) { SchedulerHelper.scheduleNextAlarm(any(), any()) }
    }
}
