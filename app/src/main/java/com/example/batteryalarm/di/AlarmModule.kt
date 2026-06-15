package com.example.batteryalarm.di

import android.content.Context
import com.example.batteryalarm.alarm.AndroidBatteryLowReceiverRegistrar
import com.example.batteryalarm.alarm.AndroidAlarmNotifier
import com.example.batteryalarm.alarm.AndroidAlarmSoundPlayer
import com.example.batteryalarm.alarm.AndroidAlarmVibrator
import com.example.batteryalarm.alarm.BatteryLowReceiverRegistrar
import com.example.batteryalarm.domain.AlarmController
import com.example.batteryalarm.domain.AlarmNotifier
import com.example.batteryalarm.domain.AlarmSettingsRepository
import com.example.batteryalarm.domain.AlarmSoundPlayer
import com.example.batteryalarm.domain.AlarmVibrator
import com.example.batteryalarm.domain.DefaultAlarmController
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
    fun provideBatteryLowReceiverRegistrar(
        @ApplicationContext context: Context,
    ): BatteryLowReceiverRegistrar = AndroidBatteryLowReceiverRegistrar(context)

    @Provides
    @Singleton
    fun provideAlarmSoundPlayer(
        @ApplicationContext context: Context,
    ): AlarmSoundPlayer = AndroidAlarmSoundPlayer(context)

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
}
