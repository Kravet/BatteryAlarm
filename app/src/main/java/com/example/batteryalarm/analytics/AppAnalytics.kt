package com.example.batteryalarm.analytics

interface AppAnalytics {
    fun logAlarmToggled(enabled: Boolean)

    fun logNotificationPermissionDenied()

    fun logTestAlarmStarted()

    fun logTestAlarmFailed()

    fun logAlarmStarted(reason: String, hourOfDay: Int)

    fun logAlarmStartBlocked(result: String)

    fun logAlarmStopped(reason: String, durationSec: Long?)

    fun logUpdateAvailableShown(version: String)

    fun logUpdateClicked(version: String)
}
