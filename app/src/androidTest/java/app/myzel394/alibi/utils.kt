package app.myzel394.alibi

import android.view.View
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isRoot

fun waitFor(delay: Long): ViewAction {
    return object : ViewAction {
        override fun getConstraints() = isRoot()
        override fun getDescription(): String = "wait for $delay milliseconds"
        override fun perform(uiController: UiController, v: View?) {
            uiController.loopMainThreadForAtLeast(delay)
        }
    }
}

fun finishTutorial(composeTestRule: ComposeContentTestRule) {
    composeTestRule.onNodeWithTag("ExplanationContinueButton").performClick()
    composeTestRule.onNodeWithTag("ResponsibilityContinueButton").performClick()
}