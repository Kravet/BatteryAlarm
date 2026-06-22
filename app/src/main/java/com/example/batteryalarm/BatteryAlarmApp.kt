package com.example.batteryalarm

import android.app.Application
import android.util.Log
import com.example.batteryalarm.domain.AlarmSettingsCoordinator
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class BatteryAlarmApp : Application() {
    @Inject
    lateinit var alarmSettingsCoordinator: AlarmSettingsCoordinator

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Application onCreate")
        Log.d(TAG, "Starting alarm settings coordinator")
        alarmSettingsCoordinator.start()
    }

    private companion object {
        const val TAG = "BatteryAlarmApp"
    }
}
