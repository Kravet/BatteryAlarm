package com.example.batteryalarm

import android.app.Application
import com.example.batteryalarm.alarm.AlarmSettingsCoordinator
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class BatteryAlarmApp : Application() {
    @Inject
    lateinit var alarmSettingsCoordinator: AlarmSettingsCoordinator

    override fun onCreate() {
        super.onCreate()
        alarmSettingsCoordinator.start()
    }
}
