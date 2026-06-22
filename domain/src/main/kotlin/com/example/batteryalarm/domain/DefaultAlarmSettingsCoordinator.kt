package com.example.batteryalarm.domain

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

class DefaultAlarmSettingsCoordinator(
    private val alarmSettingsRepository: AlarmSettingsRepository,
    private val batteryLowMonitoring: BatteryLowMonitoring,
    private val alarmController: AlarmController,
    private val scope: CoroutineScope,
) : AlarmSettingsCoordinator {
    private var started = false

    @Synchronized
    override fun start() {
        if (started) {
            return
        }
        started = true

        scope.launch {
            alarmSettingsRepository.alarmEnabled
                .distinctUntilChanged()
                .catch { exception ->
                    if (exception is CancellationException) {
                        throw exception
                    }
                }
                .collect { enabled ->
                    syncAlarmSetting(enabled)
                }
        }
    }

    private fun syncAlarmSetting(enabled: Boolean) {
        batteryLowMonitoring.setEnabled(enabled)
        if (!enabled) {
            alarmController.stopAlarm(AlarmStopReason.SettingsDisabled)
        }
    }
}
