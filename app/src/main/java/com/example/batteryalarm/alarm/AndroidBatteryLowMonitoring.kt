package com.example.batteryalarm.alarm

import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.batteryalarm.domain.BatteryLowMonitoring
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidBatteryLowMonitoring @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
) : BatteryLowMonitoring {
    override fun setEnabled(enabled: Boolean) {
        Log.d(TAG, "Battery low monitoring enabled=$enabled")
        if (enabled) {
            Log.d(TAG, "Requesting foreground monitoring service start")
            ContextCompat.startForegroundService(
                appContext,
                BatteryLowMonitoringService.startIntent(appContext),
            )
        } else {
            Log.d(TAG, "Requesting foreground monitoring service stop")
            appContext.stopService(BatteryLowMonitoringService.serviceIntent(appContext))
        }
    }

    private companion object {
        const val TAG = "BatteryLowMonitoring"
    }
}
