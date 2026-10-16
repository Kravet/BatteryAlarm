package com.example.batteryalarm.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.batteryalarm.ui.privacy.PrivacyBottomSheetTestTags
import com.example.batteryalarm.ui.privacy.PrivacySheetMode
import com.example.batteryalarm.ui.theme.BatteryAlarmTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PrivacyBottomSheetTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun onboarding_shows_balanced_actions_and_customize() {
        var allowCount = 0
        var declineCount = 0
        composeRule.setContent {
            BatteryAlarmTheme {
                MainScreen(
                    uiState = MainUiState.from(
                        alarmEnabled = false,
                        isTestAlarmPending = false,
                        privacySheetMode = PrivacySheetMode.ONBOARDING,
                    ),
                    snackbarHostState = remember { SnackbarHostState() },
                    onAlarmToggleClick = {},
                    onTestAlarmClick = {},
                    onAllowBothPrivacy = { allowCount += 1 },
                    onDeclineBothPrivacy = { declineCount += 1 },
                )
            }
        }

        composeRule.onNodeWithTag(PrivacyBottomSheetTestTags.TITLE).assertIsDisplayed()
        composeRule.onNodeWithTag(PrivacyBottomSheetTestTags.ALLOW_BOTH).assertIsDisplayed()
        composeRule.onNodeWithTag(PrivacyBottomSheetTestTags.DECLINE_BOTH).assertIsDisplayed()
        composeRule.onNodeWithTag(PrivacyBottomSheetTestTags.CUSTOMIZE).assertIsDisplayed()
        composeRule.onNodeWithTag(PrivacyBottomSheetTestTags.PRIVACY_POLICY_LINK).assertIsDisplayed()

        composeRule.onNodeWithTag(PrivacyBottomSheetTestTags.ALLOW_BOTH).performClick()
        composeRule.onNodeWithTag(PrivacyBottomSheetTestTags.DECLINE_BOTH).performClick()
        assertEquals(1, allowCount)
        assertEquals(1, declineCount)
    }

    @Test
    fun onboarding_customize_defaults_switches_off_and_saves_choices() {
        var savedAnalytics: Boolean? = null
        var savedCrashlytics: Boolean? = null
        composeRule.setContent {
            BatteryAlarmTheme {
                MainScreen(
                    uiState = MainUiState.from(
                        alarmEnabled = false,
                        isTestAlarmPending = false,
                        privacySheetMode = PrivacySheetMode.ONBOARDING,
                    ),
                    snackbarHostState = remember { SnackbarHostState() },
                    onAlarmToggleClick = {},
                    onTestAlarmClick = {},
                    onSaveCustomPrivacyChoices = { analytics, crashlytics ->
                        savedAnalytics = analytics
                        savedCrashlytics = crashlytics
                    },
                )
            }
        }

        composeRule.onNodeWithTag(PrivacyBottomSheetTestTags.CUSTOMIZE).performClick()
        composeRule.onNodeWithTag(PrivacyBottomSheetTestTags.ANALYTICS_SWITCH).assertIsOff()
        composeRule.onNodeWithTag(PrivacyBottomSheetTestTags.CRASHLYTICS_SWITCH).assertIsOff()
        composeRule.onNodeWithTag(PrivacyBottomSheetTestTags.ANALYTICS_SWITCH).performClick()
        composeRule.onNodeWithTag(PrivacyBottomSheetTestTags.ANALYTICS_SWITCH).assertIsOn()
        composeRule.onNodeWithTag(PrivacyBottomSheetTestTags.SAVE_CHOICES).performClick()

        assertEquals(true, savedAnalytics)
        assertEquals(false, savedCrashlytics)
    }

    @Test
    fun settings_mode_shows_persisted_switches_without_allow_decline() {
        var analyticsChanges = 0
        composeRule.setContent {
            var analytics by remember { mutableStateOf(true) }
            var crashlytics by remember { mutableStateOf(false) }
            BatteryAlarmTheme {
                MainScreen(
                    uiState = MainUiState.from(
                        alarmEnabled = true,
                        isTestAlarmPending = false,
                        decisionRecorded = true,
                        analyticsEnabled = analytics,
                        crashlyticsEnabled = crashlytics,
                        privacySheetMode = PrivacySheetMode.SETTINGS,
                    ),
                    snackbarHostState = remember { SnackbarHostState() },
                    onAlarmToggleClick = {},
                    onTestAlarmClick = {},
                    onAnalyticsConsentChange = {
                        analyticsChanges += 1
                        analytics = it
                    },
                    onCrashlyticsConsentChange = { crashlytics = it },
                )
            }
        }

        composeRule.onNodeWithTag(PrivacyBottomSheetTestTags.TITLE).assertIsDisplayed()
        composeRule.onNodeWithTag(PrivacyBottomSheetTestTags.ALLOW_BOTH).assertDoesNotExist()
        composeRule.onNodeWithTag(PrivacyBottomSheetTestTags.DECLINE_BOTH).assertDoesNotExist()
        composeRule.onNodeWithTag(PrivacyBottomSheetTestTags.ANALYTICS_SWITCH).assertIsOn()
        composeRule.onNodeWithTag(PrivacyBottomSheetTestTags.CRASHLYTICS_SWITCH).assertIsOff()

        composeRule.onNodeWithTag(PrivacyBottomSheetTestTags.ANALYTICS_SWITCH).performClick()
        assertTrue(analyticsChanges >= 1)
    }
}
