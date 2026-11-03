package com.example.batteryalarm.di

import com.example.batteryalarm.domain.AlarmController
import com.example.batteryalarm.domain.AlarmDomainFactory
import com.example.batteryalarm.domain.AlarmNotifier
import com.example.batteryalarm.domain.AlarmSettingsCoordinator
import com.example.batteryalarm.domain.AlarmSettingsRepository
import com.example.batteryalarm.domain.AlarmSoundPlayer
import com.example.batteryalarm.domain.AlarmSoundSession
import com.example.batteryalarm.domain.AlarmVibrator
import com.example.batteryalarm.domain.BatteryLowMonitoring
import com.example.batteryalarm.domain.update.AppUpdateChecker
import com.example.batteryalarm.domain.update.InstalledAppVersionName
import com.example.batteryalarm.domain.update.LatestStableReleaseSource
import com.example.batteryalarm.domain.update.ReleasePageUrlPolicy
import com.example.batteryalarm.domain.update.UpdateCheckAttemptStore
import com.example.batteryalarm.domain.update.UpdateCheckerEnabled
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope

@Module
@InstallIn(SingletonComponent::class)
object DomainBindingsModule {
    @Provides
    @Singleton
    @RawAlarmController
    fun provideRawAlarmController(
        settingsRepository: AlarmSettingsRepository,
        soundPlayer: AlarmSoundPlayer,
        vibrator: AlarmVibrator,
        notifier: AlarmNotifier,
    ): AlarmController = AlarmDomainFactory.createAlarmController(
        settingsRepository = settingsRepository,
        soundPlayer = soundPlayer,
        vibrator = vibrator,
        notifier = notifier,
    )

    @Provides
    @Singleton
    fun provideAlarmSettingsCoordinator(
        alarmSettingsRepository: AlarmSettingsRepository,
        batteryLowMonitoring: BatteryLowMonitoring,
        alarmController: AlarmController,
        @ApplicationScope scope: CoroutineScope,
    ): AlarmSettingsCoordinator = AlarmDomainFactory.createAlarmSettingsCoordinator(
        settingsRepository = alarmSettingsRepository,
        batteryLowMonitoring = batteryLowMonitoring,
        alarmController = alarmController,
        scope = scope,
    )

    @Provides
    @Singleton
    fun provideAlarmSoundSession(
        @ApplicationScope scope: CoroutineScope,
    ): AlarmSoundSession = AlarmDomainFactory.createAlarmSoundSession(scope)

    @Provides
    @Singleton
    fun provideAppUpdateChecker(
        isEnabled: UpdateCheckerEnabled,
        attemptStore: UpdateCheckAttemptStore,
        releaseSource: LatestStableReleaseSource,
        installedVersionName: InstalledAppVersionName,
        releasePageUrlPolicy: ReleasePageUrlPolicy,
    ): AppUpdateChecker = AlarmDomainFactory.createAppUpdateChecker(
        isEnabled = isEnabled,
        attemptStore = attemptStore,
        releaseSource = releaseSource,
        installedVersionName = installedVersionName,
        releasePageUrlPolicy = releasePageUrlPolicy,
    )
}
