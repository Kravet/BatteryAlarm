package com.example.batteryalarm.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.batteryalarm.ui.theme.BatteryAlarmTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Emulator-visible UI tests: mock “new version available” and assert the inline update
 * button on MainScreen. No real GitHub API.
 */
@RunWith(AndroidJUnit4::class)
class UpdateAvailableButtonTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val fakeUpdate = AppUpdateUiModel(
        versionName = "1.1.0",
        releaseNotes = "Improved battery monitoring",
        releasePageUrl = "https://github.com/example/repo/releases/tag/v1.1.0",
    )

    @Test
    fun when_new_version_is_mocked_then_update_button_is_shown_on_main_screen() {
        composeRule.setContent {
            BatteryAlarmTheme {
                MainScreen(
                    uiState = MainUiState.from(
                        alarmEnabled = false,
                        isTestAlarmPending = false,
                        pendingUpdate = fakeUpdate,
                    ),
                    snackbarHostState = remember { SnackbarHostState() },
                    onAlarmToggleClick = {},
                    onTestAlarmClick = {},
                )
            }
        }

        composeRule.onNodeWithText("Battery Alarm 1.1.0 is available").assertIsDisplayed()
        composeRule.onNodeWithText("A new version of Battery Alarm is available.").assertIsDisplayed()
        composeRule.onNodeWithText("Update").assertIsDisplayed()
        composeRule.onNodeWithText("Test alarm").assertIsDisplayed()
    }

    @Test
    fun when_no_pending_update_then_update_button_is_hidden() {
        composeRule.setContent {
            BatteryAlarmTheme {
                MainScreen(
                    uiState = MainUiState.from(
                        alarmEnabled = false,
                        isTestAlarmPending = false,
                        pendingUpdate = null,
                    ),
                    snackbarHostState = remember { SnackbarHostState() },
                    onAlarmToggleClick = {},
                    onTestAlarmClick = {},
                )
            }
        }

        composeRule.onNodeWithText("Battery Alarm 1.1.0 is available").assertDoesNotExist()
        composeRule.onNodeWithText("Update").assertDoesNotExist()
        composeRule.onNodeWithText("Test alarm").assertIsDisplayed()
    }

    @Test
    fun update_invokes_action_with_release_url_and_hides_button() {
        var openedUrl: String? = null
        composeRule.setContent {
            BatteryAlarmTheme {
                var pending by remember { mutableStateOf<AppUpdateUiModel?>(fakeUpdate) }
                MainScreen(
                    uiState = MainUiState.from(
                        alarmEnabled = false,
                        isTestAlarmPending = false,
                        pendingUpdate = pending,
                    ),
                    snackbarHostState = remember { SnackbarHostState() },
                    onAlarmToggleClick = {},
                    onTestAlarmClick = {},
                    onUpdateClick = {
                        openedUrl = pending?.releasePageUrl
                        pending = null
                    },
                )
            }
        }

        composeRule.onNodeWithText("Update").performClick()
        assertEquals(fakeUpdate.releasePageUrl, openedUrl)
        composeRule.onNodeWithText("Battery Alarm 1.1.0 is available").assertDoesNotExist()
    }

    @Test
    fun update_button_alone_shows_version_and_invokes_action() {
        var updateClicked = false
        composeRule.setContent {
            BatteryAlarmTheme {
                UpdateAvailableButton(
                    update = fakeUpdate,
                    onUpdateClick = { updateClicked = true },
                )
            }
        }

        composeRule.onNodeWithText("Battery Alarm 1.1.0 is available").assertIsDisplayed()
        composeRule.onNodeWithText("Update").performClick()
        assertTrue(updateClicked)
    }
}
