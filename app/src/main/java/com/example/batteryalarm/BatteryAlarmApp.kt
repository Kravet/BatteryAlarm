package com.example.batteryalarm

import android.app.Application
import android.util.Log
import com.example.batteryalarm.domain.AlarmSettingsCoordinator
import com.example.batteryalarm.privacy.FirebaseConsentCoordinator
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class BatteryAlarmApp : Application() {
    @Inject
    lateinit var alarmSettingsCoordinator: AlarmSettingsCoordinator

    @Inject
    lateinit var firebaseConsentCoordinator: FirebaseConsentCoordinator

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Application onCreate")
        Log.d(TAG, "Starting alarm settings coordinator")
        alarmSettingsCoordinator.start()
        firebaseConsentCoordinator.start()
    }

    private companion object {
        const val TAG = "BatteryAlarmApp"
    }
}
