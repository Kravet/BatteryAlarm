package com.example.batteryalarm.domain

import kotlinx.coroutines.CoroutineScope

/** Creates the default domain implementations behind their public contracts. */
object AlarmDomainFactory {
    fun createAlarmController(
        settingsRepository: AlarmSettingsRepository,
        soundPlayer: AlarmSoundPlayer,
        vibrator: AlarmVibrator,
        notifier: AlarmNotifier,
    ): AlarmController = DefaultAlarmController(
        settingsRepository = settingsRepository,
        soundPlayer = soundPlayer,
        vibrator = vibrator,
        notifier = notifier,
    )

    fun createAlarmSettingsCoordinator(
        settingsRepository: AlarmSettingsRepository,
        batteryLowMonitoring: BatteryLowMonitoring,
        alarmController: AlarmController,
        scope: CoroutineScope,
    ): AlarmSettingsCoordinator = DefaultAlarmSettingsCoordinator(
        alarmSettingsRepository = settingsRepository,
        batteryLowMonitoring = batteryLowMonitoring,
        alarmController = alarmController,
        scope = scope,
    )
}
