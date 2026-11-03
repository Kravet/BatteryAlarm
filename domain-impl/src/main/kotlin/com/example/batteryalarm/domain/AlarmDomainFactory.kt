package com.example.batteryalarm.domain

import com.example.batteryalarm.domain.update.AppUpdateChecker
import com.example.batteryalarm.domain.update.CheckForAppUpdate
import com.example.batteryalarm.domain.update.InstalledAppVersionName
import com.example.batteryalarm.domain.update.LatestStableReleaseSource
import com.example.batteryalarm.domain.update.ReleasePageUrlPolicy
import com.example.batteryalarm.domain.update.UpdateCheckAttemptStore
import com.example.batteryalarm.domain.update.UpdateCheckerEnabled
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

    fun createAlarmSoundSession(scope: CoroutineScope): AlarmSoundSession =
        DefaultAlarmSoundSession(VolumeRampController(scope))

    fun createAppUpdateChecker(
        isEnabled: UpdateCheckerEnabled,
        attemptStore: UpdateCheckAttemptStore,
        releaseSource: LatestStableReleaseSource,
        installedVersionName: InstalledAppVersionName,
        releasePageUrlPolicy: ReleasePageUrlPolicy,
    ): AppUpdateChecker = CheckForAppUpdate(
        isEnabled = isEnabled,
        attemptStore = attemptStore,
        releaseSource = releaseSource,
        installedVersionName = installedVersionName,
        releasePageUrlPolicy = releasePageUrlPolicy,
    )
}
