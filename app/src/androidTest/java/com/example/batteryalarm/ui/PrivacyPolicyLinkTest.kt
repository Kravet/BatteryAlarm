package com.example.batteryalarm.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.batteryalarm.privacy.PrivacyPolicyLinks
import com.example.batteryalarm.ui.theme.BatteryAlarmTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PrivacyPolicyLinkTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun privacy_policy_link_is_shown_on_main_screen() {
        composeRule.setContent {
            BatteryAlarmTheme {
                MainScreen(
                    uiState = MainUiState.from(alarmEnabled = false, isTestAlarmPending = false),
                    snackbarHostState = remember { SnackbarHostState() },
                    onAlarmToggleClick = {},
                    onTestAlarmClick = {},
                )
            }
        }

        composeRule.onNodeWithText("Privacy Policy").assertIsDisplayed()
        composeRule.onNodeWithText("Test alarm").assertIsDisplayed()
    }

    @Test
    fun privacy_policy_click_invokes_callback_with_public_url() {
        var openedUrl: String? = null
        composeRule.setContent {
            BatteryAlarmTheme {
                MainScreen(
                    uiState = MainUiState.from(alarmEnabled = true, isTestAlarmPending = false),
                    snackbarHostState = remember { SnackbarHostState() },
                    onAlarmToggleClick = {},
                    onTestAlarmClick = {},
                    onPrivacyPolicyClick = { openedUrl = PrivacyPolicyLinks.PUBLIC_URL },
                )
            }
        }

        composeRule.onNodeWithText("Privacy Policy").performClick()
        assertEquals(PrivacyPolicyLinks.PUBLIC_URL, openedUrl)
        assertTrue(openedUrl!!.startsWith("https://"))
    }
}
