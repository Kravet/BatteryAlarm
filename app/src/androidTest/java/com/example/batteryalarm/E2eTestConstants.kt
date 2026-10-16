package com.example.batteryalarm

object E2eTestConstants {
    const val PACKAGE_NAME = "com.kravets.batteryalarm"
    const val ALARM_TITLE = "Low battery alarm"
    const val ALARM_SCREEN_TITLE = "Low Battery"
    const val DISMISS_LABEL = "Dismiss"
    const val STOP_LABEL = "HOLD to Stop"
    // The main screen exposes a single toggle (a Switch on the status card) whose
    // contentDescription reflects the action it performs: "Enable battery alarm" when
    // currently disabled, "Disable battery alarm" when currently enabled. These labels
    // are matched via By.desc.
    const val ENABLE_TOGGLE_DESC = "Enable battery alarm"
    const val DISABLE_TOGGLE_DESC = "Disable battery alarm"
    const val ENABLED_LABEL = DISABLE_TOGGLE_DESC
    const val DISABLED_LABEL = ENABLE_TOGGLE_DESC
    const val ENABLE_BUTTON = ENABLE_TOGGLE_DESC
    const val DISABLE_BUTTON = DISABLE_TOGGLE_DESC
    const val TEST_BUTTON = "Test alarm"
    const val PRIVACY_ONBOARDING_TITLE = "Help Improve Battery Alarm"
    const val PRIVACY_ALLOW_BOTH = "Allow Both"
    const val PRIVACY_DECLINE_BOTH = "Decline Both"
    const val PRIVACY_CUSTOMIZE = "Customize choices"
    const val PRIVACY_SAVE_CHOICES = "Save Choices"
    const val PRIVACY_DIAGNOSTICS = "Privacy & Diagnostics"
    const val PRIVACY_ANALYTICS_TITLE = "Usage Analytics"
    const val PRIVACY_CRASHLYTICS_TITLE = "Crash Reports"
    const val PRIVACY_POLICY_LINK = "Privacy Policy"
    const val TIMEOUT_MS = 5_000L
    const val ALARM_ASSERT_TIMEOUT_MS = 15_000L
    const val MONITORING_RESTART_TIMEOUT_MS = 15_000L
    const val TOGGLE_SETTLE_TIMEOUT_MS = 15_000L
}
