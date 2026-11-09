package com.example.batteryalarm.analytics

class FakeAppAnalytics : AppAnalytics {
    val alarmToggled = mutableListOf<Boolean>()
    var notificationPermissionDeniedCount = 0
    var testAlarmStartedCount = 0
    var testAlarmFailedCount = 0
    val alarmStarted = mutableListOf<Pair<String, Int>>()
    val alarmStartBlocked = mutableListOf<String>()
    val alarmStopped = mutableListOf<Pair<String, Long?>>()
    var batteryLowReceivedCount = 0
    var powerConnectedReceivedCount = 0
    val alarmScreenForeground = mutableListOf<String>()
    val alarmDismissRequested = mutableListOf<String>()
    val alarmOutputResults = mutableListOf<Pair<String, String>>()
    val monitoringStates = mutableListOf<String>()
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

    override fun logBatteryLowReceived() {
        batteryLowReceivedCount += 1
    }

    override fun logPowerConnectedReceived() {
        powerConnectedReceivedCount += 1
    }

    override fun logAlarmScreenForeground(reason: String) {
        alarmScreenForeground += reason
    }

    override fun logAlarmDismissRequested(source: String) {
        alarmDismissRequested += source
    }

    override fun logAlarmOutputResult(output: String, status: String) {
        alarmOutputResults += output to status
    }

    override fun logMonitoringStateChanged(state: String) {
        monitoringStates += state
    }

    override fun logUpdateAvailableShown(version: String) {
        updateAvailableShown += version
    }

    override fun logUpdateClicked(version: String) {
        updateClicked += version
    }
}
