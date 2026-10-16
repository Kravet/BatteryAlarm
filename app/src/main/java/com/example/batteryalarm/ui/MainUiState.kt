package com.example.batteryalarm.ui

import androidx.annotation.StringRes
import com.example.batteryalarm.R
import com.example.batteryalarm.ui.privacy.PrivacySheetMode

data class MainUiState(
    val isEnabled: Boolean,
    @param:StringRes val toggleContentDescriptionRes: Int,
    val isTestAlarmPending: Boolean,
    val testAlarmSecondsRemaining: Int? = null,
    val pendingUpdate: AppUpdateUiModel? = null,
    val analyticsEnabled: Boolean = false,
    val crashlyticsEnabled: Boolean = false,
    val decisionRecorded: Boolean = false,
    val privacySheetMode: PrivacySheetMode? = null,
) {
    val isPrivacySheetVisible: Boolean get() = privacySheetMode != null

    companion object {
        fun from(
            alarmEnabled: Boolean,
            isTestAlarmPending: Boolean,
            testAlarmSecondsRemaining: Int? = null,
            pendingUpdate: AppUpdateUiModel? = null,
            analyticsEnabled: Boolean = false,
            crashlyticsEnabled: Boolean = false,
            decisionRecorded: Boolean = false,
            privacySheetMode: PrivacySheetMode? = null,
        ): MainUiState = MainUiState(
            isEnabled = alarmEnabled,
            toggleContentDescriptionRes = if (alarmEnabled) {
                R.string.disable_battery_alarm
            } else {
                R.string.enable_battery_alarm
            },
            isTestAlarmPending = isTestAlarmPending,
            testAlarmSecondsRemaining = testAlarmSecondsRemaining,
            pendingUpdate = pendingUpdate,
            analyticsEnabled = analyticsEnabled,
            crashlyticsEnabled = crashlyticsEnabled,
            decisionRecorded = decisionRecorded,
            privacySheetMode = privacySheetMode,
        )
    }
}
