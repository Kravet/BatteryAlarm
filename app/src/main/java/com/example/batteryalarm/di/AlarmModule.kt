package com.example.batteryalarm.di

import android.content.Context
import com.example.batteryalarm.alarm.AndroidAlarmNotifier
import com.example.batteryalarm.alarm.AndroidAlarmSoundPlayer
import com.example.batteryalarm.alarm.AndroidAlarmVibrator
import com.example.batteryalarm.alarm.AndroidBatteryLowMonitoring
import com.example.batteryalarm.analytics.AnalyticsAlarmController
import com.example.batteryalarm.analytics.AppAnalytics
import com.example.batteryalarm.domain.AlarmController
import com.example.batteryalarm.domain.AlarmNotifier
import com.example.batteryalarm.domain.AlarmSettingsRepository
import com.example.batteryalarm.domain.AlarmSoundPlayer
import com.example.batteryalarm.domain.AlarmSoundSession
import com.example.batteryalarm.domain.AlarmVibrator
import com.example.batteryalarm.domain.BatteryLowMonitoring
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
    fun provideAlarmSoundPlayer(
        @ApplicationContext context: Context,
        session: AlarmSoundSession,
    ): AlarmSoundPlayer = AndroidAlarmSoundPlayer(
        context = context,
        session = session,
    )

    @Provides
    @Singleton
    fun provideAlarmVibrator(
        @ApplicationContext context: Context,
    ): AlarmVibrator = AndroidAlarmVibrator(context)

    @Provides
    @Singleton
    fun provideAlarmNotifier(
        @ApplicationContext context: Context,
    ): AlarmNotifier = AndroidAlarmNotifier(context)

    @Provides
    @Singleton
    fun provideAlarmController(
        @RawAlarmController delegate: AlarmController,
        analytics: AppAnalytics,
    ): AlarmController = AnalyticsAlarmController(
        delegate = delegate,
        analytics = analytics,
    )
}
