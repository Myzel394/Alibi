package app.myzel394.alibi

import android.accessibilityservice.AccessibilityService
import android.os.Build
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.rule.GrantPermissionRule.grant
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class MicrophoneRecordingTest {
    private val permissions = arrayOf(
        android.Manifest.permission.RECORD_AUDIO,
        android.Manifest.permission.CAMERA,
    )

    @get:Rule
    val composeTestRule = createComposeRule()

    @JvmField
    @Rule
    val permissionRule: GrantPermissionRule =
        grant(*permissions)

    @Test
    fun runMicrophoneTest() {
        composeTestRule.setContent {
            AlibiApp()
        }

        finishTutorial(composeTestRule)

        composeTestRule.onNodeWithTag("AudioRecorderStartButton").performClick()
        onView(isRoot()).perform(waitFor(1000))
        composeTestRule.onNodeWithTag("SaveButton").performClick()
        onView(isRoot()).perform(waitFor(1000))

        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).pressBack()

        composeTestRule.onNodeWithTag("AudioRecorderStartButton").assertIsDisplayed()
    }
}
