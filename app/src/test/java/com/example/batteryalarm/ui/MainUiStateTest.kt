package com.example.batteryalarm.ui

import com.example.batteryalarm.R
import com.example.batteryalarm.ui.theme.AlarmDisableRed
import com.example.batteryalarm.ui.theme.AlarmEnableGreen
import org.junit.Assert.assertEquals
import org.junit.Test

class MainUiStateTest {
    @Test
    fun `when alarm is enabled, then enabled ui state`() {
        val state = MainUiState.from(
            alarmEnabled = true,
            isTestAlarmPending = false,
        )

        assertEquals(
            MainUiState(
                isEnabled = true,
                statusTextRes = R.string.alarm_status_enabled,
                toggleButtonTextRes = R.string.alarm_toggle_off,
                toggleContentDescriptionRes = R.string.disable_battery_alarm,
                toggleButtonColor = AlarmDisableRed,
                isTestAlarmPending = false,
            ),
            state,
        )
    }

    @Test
    fun `when alarm is disabled, then from returns disabled ui state`() {
        val state = MainUiState.from(
            alarmEnabled = false,
            isTestAlarmPending = false,
        )

        assertEquals(
            MainUiState(
                isEnabled = false,
                statusTextRes = R.string.alarm_status_disabled,
                toggleButtonTextRes = R.string.alarm_toggle_on,
                toggleContentDescriptionRes = R.string.enable_battery_alarm,
                toggleButtonColor = AlarmEnableGreen,
                isTestAlarmPending = false,
            ),
            state,
        )
    }

    @Test
    fun `when alarm is enabled and test alarm is pending, then isTestAlarmPending is true`() {
        val state = MainUiState.from(
            alarmEnabled = true,
            isTestAlarmPending = true,
        )

        assertEquals(true, state.isTestAlarmPending)
    }

    @Test
    fun `when alarm is disabled and test alarm is pending, then isTestAlarmPending is true`() {
        val state = MainUiState.from(
            alarmEnabled = false,
            isTestAlarmPending = true,
        )

        assertEquals(true, state.isTestAlarmPending)
    }
}
