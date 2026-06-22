package com.example.batteryalarm

import android.os.SystemClock

object E2eAlarmLog {
    private const val MONITORING_TAG = "BatteryLowMonitoringSvc"
    private const val HANDLER_TAG = "BatteryLowAlarmHandler"
    private const val NOTIFIER_TAG = "AndroidAlarmNotifier"

    fun awaitMonitoringStarted(runShell: (String) -> String) {
        awaitLog(
            runShell = runShell,
            tag = MONITORING_TAG,
            contains = "Battery low monitoring started",
            description = "foreground monitoring service did not start",
        )
    }

    fun awaitBatteryLowHandled(runShell: (String) -> String) {
        awaitLog(
            runShell = runShell,
            tag = MONITORING_TAG,
            contains = "ACTION_BATTERY_LOW received",
            description = "monitoring service did not receive ACTION_BATTERY_LOW",
        )
    }

    fun awaitSystemAlarmStarted(runShell: (String) -> String) {
        awaitAnyLog(
            runShell = runShell,
            options = listOf(
                LogExpectation(
                    description = "battery low handled",
                    filter = "$HANDLER_TAG:D",
                    contains = "startAlarm reason=SystemLowBattery",
                ),
                LogExpectation(
                    description = "alarm notification posted",
                    filter = "$NOTIFIER_TAG:D",
                    contains = "Posted alarm notification",
                ),
            ),
        )
    }

    fun awaitTestAlarmStarted(runShell: (String) -> String) {
        awaitAnyLog(
            runShell = runShell,
            options = listOf(
                LogExpectation(
                    description = "test alarm handled",
                    filter = "$HANDLER_TAG:D",
                    contains = "startAlarm reason=TestAlarmFlow",
                ),
                LogExpectation(
                    description = "test alarm notification posted",
                    filter = "$NOTIFIER_TAG:D",
                    contains = "Posted alarm notification, reason=TestAlarmFlow",
                ),
            ),
        )
    }

    private data class LogExpectation(
        val description: String,
        val filter: String,
        val contains: String,
    )

    private fun awaitLog(
        runShell: (String) -> String,
        tag: String,
        contains: String,
        description: String,
        timeoutMs: Long = 10_000L,
    ) {
        val deadline = SystemClock.uptimeMillis() + timeoutMs
        while (SystemClock.uptimeMillis() < deadline) {
            val logs = runShell("logcat -d -s $tag:D")
            if (logs.contains(contains)) {
                return
            }
            SystemClock.sleep(300)
        }
        error("$description; expected log in $tag containing: $contains")
    }

    private fun awaitAnyLog(
        runShell: (String) -> String,
        options: List<LogExpectation>,
        timeoutMs: Long = 15_000L,
    ) {
        val deadline = SystemClock.uptimeMillis() + timeoutMs
        while (SystemClock.uptimeMillis() < deadline) {
            for (option in options) {
                val logs = runShell("logcat -d -s ${option.filter}")
                if (logs.contains(option.contains)) {
                    return
                }
            }
            SystemClock.sleep(300)
        }
        val optionNames = options.joinToString { it.description }
        error("Alarm pipeline did not complete; expected one of: $optionNames")
    }
}
