package com.example.batteryalarm.analytics

interface AppAnalytics {
    fun logAlarmToggled(enabled: Boolean)

    fun logNotificationPermissionDenied()

    fun logTestAlarmStarted()

    fun logTestAlarmFailed()

    fun logAlarmStarted(reason: String, hourOfDay: Int)

    fun logAlarmStartBlocked(result: String)

    fun logAlarmStopped(reason: String, durationSec: Long?)

    fun logBatteryLowReceived()

    fun logPowerConnectedReceived()

    fun logAlarmScreenForeground(reason: String)

    fun logAlarmDismissRequested(source: String)

    fun logAlarmOutputResult(output: String, status: String)

    fun logMonitoringStateChanged(state: String)

    fun logUpdateAvailableShown(version: String)

    fun logUpdateClicked(version: String)
}

inline fun AppAnalytics.recordSafely(record: AppAnalytics.() -> Unit) {
    try {
        record()
    } catch (_: Exception) {
        // Diagnostics must not affect alarm behavior.
    }
}
