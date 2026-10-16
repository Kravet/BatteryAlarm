package com.example.batteryalarm.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.batteryalarm.privacy.PrivacyPolicyLinks
import com.example.batteryalarm.ui.privacy.PrivacyBottomSheetTestTags
import com.example.batteryalarm.ui.privacy.PrivacySheetMode
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
    fun privacy_diagnostics_entry_is_shown_on_main_screen() {
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

        composeRule.onNodeWithText("Privacy & Diagnostics").assertIsDisplayed()
        composeRule.onNodeWithText("Test alarm").assertIsDisplayed()
    }

    @Test
    fun settings_privacy_sheet_shows_privacy_policy_link() {
        var openedUrl: String? = null
        composeRule.setContent {
            BatteryAlarmTheme {
                MainScreen(
                    uiState = MainUiState.from(
                        alarmEnabled = true,
                        isTestAlarmPending = false,
                        decisionRecorded = true,
                        privacySheetMode = PrivacySheetMode.SETTINGS,
                    ),
                    snackbarHostState = remember { SnackbarHostState() },
                    onAlarmToggleClick = {},
                    onTestAlarmClick = {},
                    onPrivacyPolicyClick = { openedUrl = PrivacyPolicyLinks.PUBLIC_URL },
                )
            }
        }

        composeRule.onNodeWithTag(PrivacyBottomSheetTestTags.PRIVACY_POLICY_LINK).assertIsDisplayed()
        composeRule.onNodeWithTag(PrivacyBottomSheetTestTags.PRIVACY_POLICY_LINK).performClick()
        assertEquals(PrivacyPolicyLinks.PUBLIC_URL, openedUrl)
        assertTrue(openedUrl!!.startsWith("https://"))
    }
}
