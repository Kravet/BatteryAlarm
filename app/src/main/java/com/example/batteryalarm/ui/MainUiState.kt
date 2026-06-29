package com.example.batteryalarm.ui

import androidx.annotation.StringRes
import com.example.batteryalarm.R

data class MainUiState(
    val isEnabled: Boolean,
    @param:StringRes val toggleContentDescriptionRes: Int,
    val isTestAlarmPending: Boolean,
    val testAlarmSecondsRemaining: Int? = null,
) {
    companion object {
        fun from(
            alarmEnabled: Boolean,
            isTestAlarmPending: Boolean,
            testAlarmSecondsRemaining: Int? = null,
        ): MainUiState = MainUiState(
            isEnabled = alarmEnabled,
            toggleContentDescriptionRes = if (alarmEnabled) {
                R.string.disable_battery_alarm
            } else {
                R.string.enable_battery_alarm
            },
            isTestAlarmPending = isTestAlarmPending,
            testAlarmSecondsRemaining = testAlarmSecondsRemaining,
        )
    }
}
