package com.example.batteryalarm.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.batteryalarm.ui.theme.BatteryAlarmTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies main screen scroll behavior: content stays put when it fits, and on a very
 * small viewport the user can scroll down to reach the Test alarm action.
 */
@RunWith(AndroidJUnit4::class)
class MainScreenScrollTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun onTallScreen_contentFitsWithoutScrollRange() {
        setMainScreen(heightDp = TallScreenHeightDp)

        composeRule.onNodeWithText("Battery Alarm").assertIsDisplayed()
        composeRule.onNodeWithText("Test alarm").assertIsDisplayed()
        assertEquals(0f, verticalScrollMaxValue())
    }

    @Test
    fun onVerySmallScreen_testAlarmIsReachableByScrollingDown() {
        setMainScreen(heightDp = TinyScreenHeightDp)

        composeRule.onNodeWithTag(MainScreenTestTags.SCROLL_CONTAINER).assertExists()
        assertTrue(
            "Expected a vertical scroll range on a tiny viewport",
            verticalScrollMaxValue() > 0f,
        )
        composeRule.onNodeWithText("Test alarm").assertIsNotDisplayed()

        composeRule.onNodeWithText("Test alarm")
            .performScrollTo()
            .assertIsDisplayed()
    }

    private fun setMainScreen(heightDp: Int) {
        composeRule.setContent {
            BatteryAlarmTheme {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(heightDp.dp),
                ) {
                    MainScreen(
                        uiState = MainUiState.from(
                            alarmEnabled = false,
                            isTestAlarmPending = false,
                        ),
                        snackbarHostState = remember { SnackbarHostState() },
                        onAlarmToggleClick = {},
                        onTestAlarmClick = {},
                    )
                }
            }
        }
        composeRule.waitForIdle()
    }

    private fun verticalScrollMaxValue(): Float {
        val node = composeRule
            .onNodeWithTag(MainScreenTestTags.SCROLL_CONTAINER)
            .fetchSemanticsNode()
        val range = node.config[SemanticsProperties.VerticalScrollAxisRange]
        return range.maxValue()
    }

    private companion object {
        const val TallScreenHeightDp = 900
        const val TinyScreenHeightDp = 240
    }
}
