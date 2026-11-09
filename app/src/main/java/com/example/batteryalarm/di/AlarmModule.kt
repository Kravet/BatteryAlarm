package com.example.batteryalarm.di

import android.content.Context
import com.example.batteryalarm.alarm.AndroidAlarmNotifier
import com.example.batteryalarm.alarm.AndroidAlarmSoundPlayer
import com.example.batteryalarm.alarm.AndroidAlarmVibrator
import com.example.batteryalarm.alarm.AndroidBatteryLowMonitoring
import com.example.batteryalarm.analytics.AnalyticsAlarmController
import com.example.batteryalarm.analytics.AppAnalytics
import com.example.batteryalarm.analytics.AppErrorReporter
import com.example.batteryalarm.domain.AlarmController
import com.example.batteryalarm.domain.AlarmNotifier
import com.example.batteryalarm.domain.AlarmSettingsCoordinator
import com.example.batteryalarm.domain.AlarmSettingsRepository
import com.example.batteryalarm.domain.AlarmSoundPlayer
import com.example.batteryalarm.domain.AlarmVibrator
import com.example.batteryalarm.domain.BatteryLowMonitoring
import com.example.batteryalarm.domain.DefaultAlarmController
import com.example.batteryalarm.domain.provideDefaultAlarmSettingsCoordinator
import com.example.batteryalarm.settings.AndroidAlarmSettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@Module
@InstallIn(SingletonComponent::class)
object AlarmModule {
    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Provides
    @Singleton
    fun provideAlarmSettingsRepository(
        @ApplicationContext context: Context,
    ): AlarmSettingsRepository = AndroidAlarmSettingsRepository(context)

    @Provides
    @Singleton
    fun provideBatteryLowMonitoring(
        monitoring: AndroidBatteryLowMonitoring,
    ): BatteryLowMonitoring = monitoring

    @Provides
    @Singleton
    fun provideAlarmSettingsCoordinator(
        alarmSettingsRepository: AlarmSettingsRepository,
        batteryLowMonitoring: BatteryLowMonitoring,
        alarmController: AlarmController,
        @ApplicationScope scope: CoroutineScope,
    ): AlarmSettingsCoordinator = provideDefaultAlarmSettingsCoordinator(
        alarmSettingsRepository = alarmSettingsRepository,
        batteryLowMonitoring = batteryLowMonitoring,
        alarmController = alarmController,
        scope = scope,
    )

    @Provides
    @Singleton
    fun provideAlarmSoundPlayer(
        @ApplicationContext context: Context,
        @ApplicationScope scope: CoroutineScope,
        analytics: AppAnalytics,
        errorReporter: AppErrorReporter,
    ): AlarmSoundPlayer = AndroidAlarmSoundPlayer(
        context = context,
        scope = scope,
        analytics = analytics,
        errorReporter = errorReporter,
    )

    @Provides
    @Singleton
    fun provideAlarmVibrator(
        @ApplicationContext context: Context,
        analytics: AppAnalytics,
    ): AlarmVibrator = AndroidAlarmVibrator(context, analytics)

    @Provides
    @Singleton
    fun provideAlarmNotifier(
        @ApplicationContext context: Context,
        analytics: AppAnalytics,
    ): AlarmNotifier = AndroidAlarmNotifier(context, analytics)

    @Provides
    @Singleton
    fun provideAlarmController(
        settingsRepository: AlarmSettingsRepository,
        soundPlayer: AlarmSoundPlayer,
        vibrator: AlarmVibrator,
        notifier: AlarmNotifier,
        analytics: AppAnalytics,
        errorReporter: AppErrorReporter,
    ): AlarmController = AnalyticsAlarmController(
        delegate = DefaultAlarmController(
            settingsRepository = settingsRepository,
            soundPlayer = soundPlayer,
            vibrator = vibrator,
            notifier = notifier,
        ),
        analytics = analytics,
        errorReporter = errorReporter,
    )
}
