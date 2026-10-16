package com.example.batteryalarm

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.example.batteryalarm.E2eTestConstants.PRIVACY_ONBOARDING_TITLE
import com.example.batteryalarm.E2eTestConstants.TIMEOUT_MS
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * E2E for Firebase privacy consent: onboarding sheet, Allow/Decline/Customize,
 * Settings → Privacy & Diagnostics, and persistence across relaunch.
 */
@RunWith(AndroidJUnit4::class)
class PrivacyConsentE2eTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val device = UiDevice.getInstance(instrumentation)
    private lateinit var actions: E2eTestActions

    @Before
    fun setUp() {
        actions = E2eTestActions(
            context = instrumentation.targetContext,
            device = device,
            instrumentation = instrumentation,
        )
        actions.prepareCleanTestState()
        actions.clearPrivacyConsentForFreshPrompt()
    }

    @After
    fun tearDown() {
        runCatching {
            actions.wakeAndUnlock()
            actions.dismissAlarmUiIfVisible()
            actions.launchApp(handlePrivacyOnboarding = true)
        }
        device.pressHome()
    }

    @Test
    fun fresh_install_shows_onboarding_privacy_sheet() {
        actions.launchApp(handlePrivacyOnboarding = false)

        actions.waitForPrivacyOnboardingSheet()
        actions.assertPersistedPrivacyConsent(
            analyticsEnabled = false,
            crashlyticsEnabled = false,
            decisionRecorded = false,
        )
    }

    @Test
    fun allow_both_enables_services_and_does_not_reprompt_on_relaunch() {
        actions.launchApp(handlePrivacyOnboarding = false)
        actions.tapAllowBothPrivacy()
        actions.assertPersistedPrivacyConsent(
            analyticsEnabled = true,
            crashlyticsEnabled = true,
        )

        actions.openPrivacyDiagnostics()
        actions.assertPrivacyDiagnosticsSwitches(
            analyticsEnabled = true,
            crashlyticsEnabled = true,
        )
        actions.closePrivacySheetByBack()

        actions.backgroundApp()
        actions.launchApp(handlePrivacyOnboarding = false)

        assertFalse(
            "Onboarding should not reappear after Allow Both",
            device.wait(Until.hasObject(By.textContains(PRIVACY_ONBOARDING_TITLE)), 1_500L),
        )
        actions.assertPersistedPrivacyConsent(
            analyticsEnabled = true,
            crashlyticsEnabled = true,
        )
    }

    @Test
    fun decline_both_keeps_services_off_and_alarm_still_works() {
        actions.launchApp(handlePrivacyOnboarding = false)
        actions.tapDeclineBothPrivacy()
        actions.assertPersistedPrivacyConsent(
            analyticsEnabled = false,
            crashlyticsEnabled = false,
        )

        actions.disableBatteryAlarm()
        actions.enableBatteryAlarm()
        actions.assertAlarmEnabledConsistent(expectedEnabled = true)

        actions.openPrivacyDiagnostics()
        actions.assertPrivacyDiagnosticsSwitches(
            analyticsEnabled = false,
            crashlyticsEnabled = false,
        )
    }

    @Test
    fun customize_choices_saves_independent_toggles() {
        actions.launchApp(handlePrivacyOnboarding = false)
        actions.customizePrivacyChoices(analyticsEnabled = true, crashlyticsEnabled = false)
        actions.assertPersistedPrivacyConsent(
            analyticsEnabled = true,
            crashlyticsEnabled = false,
        )

        actions.openPrivacyDiagnostics()
        actions.assertPrivacyDiagnosticsSwitches(
            analyticsEnabled = true,
            crashlyticsEnabled = false,
        )
    }

    @Test
    fun settings_privacy_toggles_persist_immediately() {
        actions.launchApp(handlePrivacyOnboarding = false)
        actions.tapDeclineBothPrivacy()

        actions.openPrivacyDiagnostics()
        actions.setPrivacyDiagnosticsSwitch(
            title = E2eTestConstants.PRIVACY_ANALYTICS_TITLE,
            enabled = true,
        )
        actions.setPrivacyDiagnosticsSwitch(
            title = E2eTestConstants.PRIVACY_CRASHLYTICS_TITLE,
            enabled = true,
        )
        actions.assertPrivacyDiagnosticsSwitches(
            analyticsEnabled = true,
            crashlyticsEnabled = true,
        )
        actions.assertPersistedPrivacyConsent(
            analyticsEnabled = true,
            crashlyticsEnabled = true,
        )

        actions.setPrivacyDiagnosticsSwitch(
            title = E2eTestConstants.PRIVACY_ANALYTICS_TITLE,
            enabled = false,
        )
        actions.assertPersistedPrivacyConsent(
            analyticsEnabled = false,
            crashlyticsEnabled = true,
        )
    }

    @Test
    fun dismiss_without_decision_keeps_collection_off() {
        actions.launchApp(handlePrivacyOnboarding = false)
        actions.waitForPrivacyOnboardingSheet()
        device.pressBack()
        assertTrue(
            "Onboarding title should dismiss after back without recording consent",
            device.wait(Until.gone(By.textContains(PRIVACY_ONBOARDING_TITLE)), TIMEOUT_MS),
        )
        actions.assertPersistedPrivacyConsent(
            analyticsEnabled = false,
            crashlyticsEnabled = false,
            decisionRecorded = false,
        )
    }
}
