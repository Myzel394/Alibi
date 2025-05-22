package app.myzel394.alibi.receivers

import android.content.Context
import android.content.Intent
import app.myzel394.alibi.db.AppSettings
import app.myzel394.alibi.helpers.SchedulerHelper
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class BootCompletedReceiverTest {

    private lateinit var mockContext: Context
    private lateinit var mockIntent: Intent
    private lateinit var mockAppSettings: AppSettings

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.i(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0

        mockContext = mockk(relaxed = true)
        mockIntent = mockk(relaxed = true)
        mockAppSettings = mockk(relaxed = true)

        // Mock DataStore behavior
        val mockDataStore = mockk<androidx.datastore.core.DataStore<AppSettings>>(relaxed = true)
        every { mockContext.dataStore } returns mockDataStore
        every { mockDataStore.data } returns flowOf(mockAppSettings)

        mockkObject(SchedulerHelper)
        every { SchedulerHelper.scheduleNextAlarm(any(), any()) } just runs
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `onReceive - ACTION_BOOT_COMPLETED - scheduler enabled - reschedules alarm`() = runTest {
        every { mockIntent.action } returns Intent.ACTION_BOOT_COMPLETED
        every { mockAppSettings.schedulerEnabled } returns true

        val receiver = BootCompletedReceiver()
        receiver.onReceive(mockContext, mockIntent)

        verify(exactly = 1) { SchedulerHelper.scheduleNextAlarm(mockContext, mockAppSettings) }
    }

    @Test
    fun `onReceive - ACTION_BOOT_COMPLETED - scheduler disabled - no action`() = runTest {
        every { mockIntent.action } returns Intent.ACTION_BOOT_COMPLETED
        every { mockAppSettings.schedulerEnabled } returns false

        val receiver = BootCompletedReceiver()
        receiver.onReceive(mockContext, mockIntent)

        verify(exactly = 0) { SchedulerHelper.scheduleNextAlarm(any(), any()) }
    }

    @Test
    fun `onReceive - wrong action - no action`() = runTest {
        every { mockIntent.action } returns "SOME_OTHER_ACTION"
        // AppSettings can be anything here, it shouldn't be reached
        every { mockAppSettings.schedulerEnabled } returns true

        val receiver = BootCompletedReceiver()
        receiver.onReceive(mockContext, mockIntent)

        verify(exactly = 0) { SchedulerHelper.scheduleNextAlarm(any(), any()) }
    }
}
