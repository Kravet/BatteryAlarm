package com.example.batteryalarm.domain

import kotlinx.coroutines.CoroutineScope

fun provideDefaultAlarmSettingsCoordinator(
    alarmSettingsRepository: AlarmSettingsRepository,
    batteryLowMonitoring: BatteryLowMonitoring,
    alarmController: AlarmController,
    scope: CoroutineScope,
): AlarmSettingsCoordinator = DefaultAlarmSettingsCoordinator(
    alarmSettingsRepository = alarmSettingsRepository,
    batteryLowMonitoring = batteryLowMonitoring,
    alarmController = alarmController,
    scope = scope,
)
