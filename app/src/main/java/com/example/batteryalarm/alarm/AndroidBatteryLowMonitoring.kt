package com.example.batteryalarm.alarm

import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.batteryalarm.analytics.AppAnalytics
import com.example.batteryalarm.analytics.AppErrorReporter
import com.example.batteryalarm.analytics.recordSafely
import com.example.batteryalarm.analytics.recordSafely as recordErrorSafely
import com.example.batteryalarm.domain.BatteryLowMonitoring
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidBatteryLowMonitoring @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val analytics: AppAnalytics,
    private val errorReporter: AppErrorReporter,
) : BatteryLowMonitoring {
    override fun setEnabled(enabled: Boolean) {
        Log.d(TAG, "Battery low monitoring enabled=$enabled")
        if (enabled) {
            Log.d(TAG, "Requesting foreground monitoring service start")
            try {
                ContextCompat.startForegroundService(
                    appContext,
                    BatteryLowMonitoringService.startIntent(appContext),
                )
            } catch (exception: Exception) {
                analytics.recordSafely { logMonitoringStateChanged("initialization_failed") }
                errorReporter.recordErrorSafely("monitoring_service_request", exception)
                throw exception
            }
        } else {
            Log.d(TAG, "Requesting foreground monitoring service stop")
            appContext.stopService(BatteryLowMonitoringService.serviceIntent(appContext))
        }
    }

    private companion object {
        const val TAG = "BatteryLowMonitoring"
    }
}
