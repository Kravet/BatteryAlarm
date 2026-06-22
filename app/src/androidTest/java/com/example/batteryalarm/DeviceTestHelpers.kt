package com.example.batteryalarm

import android.os.SystemClock
import androidx.test.uiautomator.UiDevice

object DeviceTestHelpers {
    private const val UNLOCK_ATTEMPTS = 3
    private const val UNLOCK_RETRY_DELAY_MS = 300L

    fun wakeAndUnlockDevice(device: UiDevice, runShell: (String) -> String) {
        runShell("input keyevent KEYCODE_WAKEUP")
        runShell("cmd statusbar collapse")
        runShell("wm dismiss-keyguard")

        val width = device.displayWidth
        val height = device.displayHeight
        repeat(UNLOCK_ATTEMPTS) {
            if (isKeyguardDismissed(runShell)) {
                return
            }
            device.swipe(
                width / 2,
                (height * 0.85).toInt(),
                width / 2,
                (height * 0.2).toInt(),
                10,
            )
            runShell("wm dismiss-keyguard")
            SystemClock.sleep(UNLOCK_RETRY_DELAY_MS)
        }
    }

    private fun isKeyguardDismissed(runShell: (String) -> String): Boolean =
        runShell("dumpsys window").contains("isKeyguardShowing=false")

    fun isBatteryMonitoringServiceRunning(
        packageName: String,
        runShell: (String) -> String,
    ): Boolean = runShell("dumpsys activity services $packageName")
        .contains("BatteryLowMonitoringService")

    fun waitUntilBatteryMonitoringServiceIsRunning(
        packageName: String,
        runShell: (String) -> String,
        timeoutMs: Long = E2eTestConstants.TIMEOUT_MS,
    ) {
        val deadline = SystemClock.uptimeMillis() + timeoutMs
        while (SystemClock.uptimeMillis() < deadline) {
            if (isBatteryMonitoringServiceRunning(packageName, runShell)) {
                return
            }
            SystemClock.sleep(250)
        }
        error("BatteryLowMonitoringService did not start within ${timeoutMs}ms")
    }

    fun waitUntilBatteryMonitoringServiceStopped(
        packageName: String,
        runShell: (String) -> String,
        timeoutMs: Long = E2eTestConstants.TIMEOUT_MS,
    ) {
        val deadline = SystemClock.uptimeMillis() + timeoutMs
        while (SystemClock.uptimeMillis() < deadline) {
            if (!isBatteryMonitoringServiceRunning(packageName, runShell)) {
                return
            }
            SystemClock.sleep(250)
        }
        error("BatteryLowMonitoringService did not stop within ${timeoutMs}ms")
    }

    fun waitUntilBatteryMonitoringServiceRestarts(
        packageName: String,
        runShell: (String) -> String,
        timeoutMs: Long = E2eTestConstants.MONITORING_RESTART_TIMEOUT_MS,
    ) {
        SystemClock.sleep(1_000L)
        waitUntilBatteryMonitoringServiceIsRunning(
            packageName = packageName,
            runShell = runShell,
            timeoutMs = timeoutMs,
        )
    }
}
