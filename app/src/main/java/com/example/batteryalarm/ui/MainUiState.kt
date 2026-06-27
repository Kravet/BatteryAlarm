package com.example.batteryalarm.ui

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.example.batteryalarm.R
import com.example.batteryalarm.ui.theme.AlarmDisableRed
import com.example.batteryalarm.ui.theme.AlarmEnableGreen

data class MainUiState(
    val isEnabled: Boolean,
    @param:StringRes val statusTextRes: Int,
    @param:StringRes val toggleButtonTextRes: Int,
    @param:StringRes val toggleContentDescriptionRes: Int,
    val toggleButtonColor: Color,
    val isTestAlarmPending: Boolean,
    @param:StringRes val testAlarmButtonTextRes: Int = R.string.test_the_sound,
) {
    companion object {
        fun from(
            alarmEnabled: Boolean,
            isTestAlarmPending: Boolean,
        ): MainUiState {
            return if (alarmEnabled) {
                MainUiState(
                    isEnabled = true,
                    statusTextRes = R.string.alarm_status_enabled,
                    toggleButtonTextRes = R.string.alarm_toggle_off,
                    toggleContentDescriptionRes = R.string.disable_battery_alarm,
                    toggleButtonColor = AlarmDisableRed,
                    isTestAlarmPending = isTestAlarmPending,
                )
            } else {
                MainUiState(
                    isEnabled = false,
                    statusTextRes = R.string.alarm_status_disabled,
                    toggleButtonTextRes = R.string.alarm_toggle_on,
                    toggleContentDescriptionRes = R.string.enable_battery_alarm,
                    toggleButtonColor = AlarmEnableGreen,
                    isTestAlarmPending = isTestAlarmPending,
                )
            }
        }
    }
}
