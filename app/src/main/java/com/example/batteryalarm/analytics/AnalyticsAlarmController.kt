package com.example.batteryalarm.analytics

import com.example.batteryalarm.domain.AlarmController
import com.example.batteryalarm.domain.AlarmStartReason
import com.example.batteryalarm.domain.AlarmStartResult
import com.example.batteryalarm.domain.AlarmState
import com.example.batteryalarm.domain.AlarmStopReason
import com.example.batteryalarm.domain.AlarmStopResult
import kotlinx.coroutines.CancellationException
import java.util.Calendar

class AnalyticsAlarmController(
    private val delegate: AlarmController,
    private val analytics: AppAnalytics,
    private val errorReporter: AppErrorReporter,
) : AlarmController {
    override val state: AlarmState
        get() = delegate.state

    override suspend fun startAlarm(reason: AlarmStartReason): AlarmStartResult {
        val result = try {
            delegate.startAlarm(reason)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            errorReporter.recordSafely("alarm_start", exception)
            throw exception
        }
        when (result) {
            is AlarmStartResult.Started -> {
                analytics.recordSafely {
                    logAlarmStarted(
                        reason = reason.toAnalyticsValue(),
                        hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                    )
                }
            }
            is AlarmStartResult.AlreadyActive -> {
                analytics.recordSafely { logAlarmStartBlocked(RESULT_ALREADY_ACTIVE) }
            }
            AlarmStartResult.Disabled -> {
                analytics.recordSafely { logAlarmStartBlocked(RESULT_DISABLED) }
            }
            AlarmStartResult.Superseded -> {
                analytics.recordSafely { logAlarmStartBlocked(RESULT_SUPERSEDED) }
            }
        }
        return result
    }

    override fun stopAlarm(reason: AlarmStopReason): AlarmStopResult {
        val result = try {
            delegate.stopAlarm(reason)
        } catch (exception: Exception) {
            errorReporter.recordSafely("alarm_stop", exception)
            throw exception
        }
        if (result is AlarmStopResult.Stopped) {
            analytics.recordSafely {
                logAlarmStopped(reason = reason.toAnalyticsValue(), durationSec = null)
            }
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
