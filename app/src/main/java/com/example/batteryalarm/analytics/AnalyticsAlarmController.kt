package com.example.batteryalarm.analytics

import com.example.batteryalarm.domain.AlarmController
import com.example.batteryalarm.domain.AlarmStartReason
import com.example.batteryalarm.domain.AlarmStartResult
import com.example.batteryalarm.domain.AlarmState
import com.example.batteryalarm.domain.AlarmStopReason
import com.example.batteryalarm.domain.AlarmStopResult
import java.util.Calendar

class AnalyticsAlarmController(
    private val delegate: AlarmController,
    private val analytics: AppAnalytics,
) : AlarmController {
    override val state: AlarmState
        get() = delegate.state

    override suspend fun startAlarm(reason: AlarmStartReason): AlarmStartResult {
        val result = delegate.startAlarm(reason)
        when (result) {
            is AlarmStartResult.Started -> {
                analytics.logAlarmStarted(
                    reason = reason.toAnalyticsValue(),
                    hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                )
            }
            is AlarmStartResult.AlreadyActive -> {
                analytics.logAlarmStartBlocked(RESULT_ALREADY_ACTIVE)
            }
            AlarmStartResult.Disabled -> {
                analytics.logAlarmStartBlocked(RESULT_DISABLED)
            }
            AlarmStartResult.Superseded -> {
                analytics.logAlarmStartBlocked(RESULT_SUPERSEDED)
            }
        }
        return result
    }

    override fun stopAlarm(reason: AlarmStopReason): AlarmStopResult {
        val result = delegate.stopAlarm(reason)
        if (result is AlarmStopResult.Stopped) {
            analytics.logAlarmStopped(reason = reason.toAnalyticsValue(), durationSec = null)
        }
        return result
    }

    private fun AlarmStartReason.toAnalyticsValue(): String = when (this) {
        AlarmStartReason.SystemLowBattery -> "system_low_battery"
        AlarmStartReason.TestAlarmFlow -> "test"
    }

    private fun AlarmStopReason.toAnalyticsValue(): String = when (this) {
        AlarmStopReason.PowerConnected -> "power_connected"
        AlarmStopReason.SettingsDisabled -> "settings_disabled"
        AlarmStopReason.UserDismissed -> "user_dismissed"
    }

    private companion object {
        const val RESULT_ALREADY_ACTIVE = "already_active"
        const val RESULT_DISABLED = "disabled"
        const val RESULT_SUPERSEDED = "superseded"
    }
}
