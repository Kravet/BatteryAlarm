package com.example.batteryalarm.analytics

class FakeAppAnalytics : AppAnalytics {
    val alarmToggled = mutableListOf<Boolean>()
    var notificationPermissionDeniedCount = 0
    var testAlarmStartedCount = 0
    var testAlarmFailedCount = 0
    val alarmStarted = mutableListOf<Pair<String, Int>>()
    val alarmStartBlocked = mutableListOf<String>()
    val alarmStopped = mutableListOf<Pair<String, Long?>>()
    val updateAvailableShown = mutableListOf<String>()
    val updateClicked = mutableListOf<String>()

    override fun logAlarmToggled(enabled: Boolean) {
        alarmToggled += enabled
    }

    override fun logNotificationPermissionDenied() {
        notificationPermissionDeniedCount += 1
    }

    override fun logTestAlarmStarted() {
        testAlarmStartedCount += 1
    }

    override fun logTestAlarmFailed() {
        testAlarmFailedCount += 1
    }

    override fun logAlarmStarted(reason: String, hourOfDay: Int) {
        alarmStarted += reason to hourOfDay
    }

    override fun logAlarmStartBlocked(result: String) {
        alarmStartBlocked += result
    }

    override fun logAlarmStopped(reason: String, durationSec: Long?) {
        alarmStopped += reason to durationSec
    }

    override fun logUpdateAvailableShown(version: String) {
        updateAvailableShown += version
    }

    override fun logUpdateClicked(version: String) {
        updateClicked += version
    }
}
