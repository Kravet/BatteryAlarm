package com.example.batteryalarm

import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import androidx.test.uiautomator.By
import androidx.test.uiautomator.StaleObjectException
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.example.batteryalarm.E2eTestConstants.ALARM_ASSERT_TIMEOUT_MS
import com.example.batteryalarm.E2eTestConstants.ALARM_TITLE
import com.example.batteryalarm.E2eTestConstants.DISABLE_BUTTON
import com.example.batteryalarm.E2eTestConstants.DISABLED_LABEL
import com.example.batteryalarm.E2eTestConstants.DISMISS_LABEL
import com.example.batteryalarm.E2eTestConstants.ENABLE_BUTTON
import com.example.batteryalarm.E2eTestConstants.ENABLED_LABEL
import com.example.batteryalarm.E2eTestConstants.PACKAGE_NAME
import com.example.batteryalarm.E2eTestConstants.TEST_BUTTON
import com.example.batteryalarm.E2eTestConstants.TIMEOUT_MS
import com.example.batteryalarm.E2eTestConstants.TOGGLE_SETTLE_TIMEOUT_MS
import com.example.batteryalarm.settings.AndroidAlarmSettingsRepository
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

class E2eTestActions(
    private val context: Context,
    private val device: UiDevice,
    private val instrumentation: Instrumentation,
) {
    fun runShell(command: String): String = device.executeShellCommand(command)

    fun wakeAndUnlock() {
        DeviceTestHelpers.wakeAndUnlockDevice(device, ::runShell)
    }

    fun grantPermissions() {
        runShell("pm grant $PACKAGE_NAME android.permission.POST_NOTIFICATIONS")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            runShell("appops set $PACKAGE_NAME USE_FULL_SCREEN_INTENT allow")
        }
    }

    fun prepareCleanTestState(resetBattery: Boolean = false) {
        wakeAndUnlock()
        grantPermissions()
        dismissAlarmUiIfVisible()
        if (resetBattery) {
            resetBatteryState()
        }
    }

    fun establishDisabledBaseline() {
        launchApp()
        disableBatteryAlarm()
    }

    fun resetBatteryState() {
        runShell("dumpsys battery reset")
        runShell("dumpsys battery unplug")
        runShell("dumpsys battery set status 3")
        runShell("dumpsys battery set level 50")
    }

    fun resetAppSettings() {
        File(context.filesDir, "datastore/alarm_settings.preferences_pb").delete()
    }

    fun launchApp() {
        val intent = context.packageManager.getLaunchIntentForPackage(PACKAGE_NAME)
            ?: error("Could not find launch intent for $PACKAGE_NAME")
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        assertTrue(
            "App did not open",
            device.wait(Until.hasObject(By.pkg(PACKAGE_NAME).depth(0)), TIMEOUT_MS),
        )
        waitForMainScreen()
    }

    fun enableBatteryAlarm() {
        waitForMainScreen()
        dismissAlarmUiIfVisible()
        waitForMainScreen()

        if (device.hasObject(By.desc(ENABLED_LABEL))) {
            waitForMonitoringService()
            return
        }

        val enableButton = device.wait(
            Until.findObject(By.desc(ENABLE_BUTTON)),
            TIMEOUT_MS,
        ) ?: error("Enable battery alarm toggle was not visible")

        enableButton.click()

        assertTrue(
            "Battery alarm setting did not become enabled",
            device.wait(Until.hasObject(By.desc(ENABLED_LABEL)), TIMEOUT_MS),
        )
        waitForMonitoringService()
    }

    fun disableBatteryAlarm() {
        waitForMainScreen()
        dismissAlarmUiIfVisible()
        waitForMainScreen()

        if (device.hasObject(By.desc(DISABLED_LABEL))) {
            assertMonitoringServiceNotRunning()
            return
        }

        val disableButton = device.wait(
            Until.findObject(By.desc(DISABLE_BUTTON)),
            TIMEOUT_MS,
        ) ?: error("Disable battery alarm toggle was not visible")

        disableButton.click()

        assertTrue(
            "Battery alarm setting did not become disabled",
            device.wait(Until.hasObject(By.desc(DISABLED_LABEL)), TIMEOUT_MS),
        )
        waitForMonitoringServiceStopped()
    }

    fun tapTestAlarmButton() {
        val testButton = device.wait(
            Until.findObject(By.text(TEST_BUTTON)),
            TIMEOUT_MS,
        ) ?: error("Test alarm button was not visible")
        testButton.click()
    }

    fun tapAlarmToggleRapidly(count: Int) {
        require(count > 0) { "Tap count must be positive" }
        waitForMainScreen()
        val toggleBounds = findAlarmToggleButton().visibleBounds
        val centerX = toggleBounds.centerX()
        val centerY = toggleBounds.centerY()
        repeat(count) {
            device.click(centerX, centerY)
        }
    }

    fun readPersistedAlarmEnabled(): Boolean = runBlocking {
        AndroidAlarmSettingsRepository(context).isAlarmEnabled()
    }

    fun isUiAlarmEnabled(): Boolean {
        val enabledVisible = device.hasObject(By.desc(ENABLED_LABEL))
        val disabledVisible = device.hasObject(By.desc(DISABLED_LABEL))
        return when {
            enabledVisible && disabledVisible ->
                error("Both enabled and disabled status labels are visible")
            enabledVisible -> true
            disabledVisible -> false
            else -> error("Alarm status label was not visible")
        }
    }

    fun waitForAlarmToggleSettled(expectedEnabled: Boolean, timeoutMs: Long = TOGGLE_SETTLE_TIMEOUT_MS) {
        val deadline = SystemClock.uptimeMillis() + timeoutMs
        while (SystemClock.uptimeMillis() < deadline) {
            val persisted = readPersistedAlarmEnabled()
            val uiEnabled = runCatching { isUiAlarmEnabled() }.getOrNull()
            val serviceRunning = DeviceTestHelpers.isBatteryMonitoringServiceRunning(
                packageName = PACKAGE_NAME,
                runShell = ::runShell,
            )
            if (
                persisted == expectedEnabled &&
                uiEnabled == expectedEnabled &&
                serviceRunning == expectedEnabled
            ) {
                return
            }
            SystemClock.sleep(50)
            instrumentation.waitForIdleSync()
        }
        assertAlarmEnabledConsistent(expectedEnabled)
    }

    fun assertAlarmEnabledConsistent(expectedEnabled: Boolean) {
        val uiEnabled = isUiAlarmEnabled()
        val persistedEnabled = readPersistedAlarmEnabled()
        val serviceRunning = DeviceTestHelpers.isBatteryMonitoringServiceRunning(
            packageName = PACKAGE_NAME,
            runShell = ::runShell,
        )

        assertEquals("UI alarm enabled state", expectedEnabled, uiEnabled)
        assertEquals("Persisted alarm enabled state", expectedEnabled, persistedEnabled)
        assertEquals("Monitoring service running state", expectedEnabled, serviceRunning)
    }

    fun backgroundApp() {
        device.pressHome()
        device.wait(Until.gone(By.pkg(PACKAGE_NAME)), TIMEOUT_MS)
    }

    fun enterDozeAndSleep() {
        backgroundApp()
        runShell("cmd deviceidle force-idle")
        runShell("input keyevent KEYCODE_SLEEP")
    }

    fun exitDozeAndWake() {
        runShell("cmd deviceidle unforce")
        wakeAndUnlock()
    }

    fun killAppProcess() {
        // Only for scenarios that intentionally end the instrumentation process.
        runShell("am kill $PACKAGE_NAME")
    }

    fun waitForMonitoringService() {
        DeviceTestHelpers.waitUntilBatteryMonitoringServiceIsRunning(
            packageName = PACKAGE_NAME,
            runShell = ::runShell,
        )
    }

    fun waitForMonitoringServiceStopped() {
        DeviceTestHelpers.waitUntilBatteryMonitoringServiceStopped(
            packageName = PACKAGE_NAME,
            runShell = ::runShell,
        )
    }

    fun waitForMonitoringServiceRestart() {
        DeviceTestHelpers.waitUntilBatteryMonitoringServiceRestarts(
            packageName = PACKAGE_NAME,
            runShell = ::runShell,
        )
    }

    fun assertMonitoringServiceRunning() {
        assertTrue(
            "BatteryLowMonitoringService should be running",
            DeviceTestHelpers.isBatteryMonitoringServiceRunning(PACKAGE_NAME, ::runShell),
        )
    }

    fun assertMonitoringServiceNotRunning() {
        assertFalse(
            "BatteryLowMonitoringService should not be running",
            DeviceTestHelpers.isBatteryMonitoringServiceRunning(PACKAGE_NAME, ::runShell),
        )
    }

    fun triggerSystemBatteryLow() {
        runShell("dumpsys battery set level 50")
        runShell("dumpsys battery set level 10")
    }

    fun assertAlarmIsActive(timeoutMs: Long = ALARM_ASSERT_TIMEOUT_MS) {
        val deadline = SystemClock.uptimeMillis() + timeoutMs
        while (SystemClock.uptimeMillis() < deadline) {
            if (isAlarmActiveOnScreen()) {
                return
            }
            if (isAlarmNotificationVisible()) {
                return
            }
            SystemClock.sleep(500)
        }

        assertTrue(
            "Alarm UI was not visible via notification or full-screen overlay",
            isAlarmActiveOnScreen() || isAlarmNotificationVisible(),
        )
    }

    fun assertAlarmUiNotVisible() {
        assertFalse(
            "Low battery alarm screen was visible but should not be",
            device.wait(Until.hasObject(By.text(ALARM_TITLE)), TIMEOUT_MS),
        )
        device.openNotification()
        assertFalse(
            "Low battery alarm notification was visible but should not be",
            device.wait(Until.hasObject(By.text(ALARM_TITLE)), TIMEOUT_MS),
        )
        device.pressBack()
    }

    fun dismissAlarmUiIfVisible() {
        if (clickTextIfVisible(DISMISS_LABEL, 1_000L)) {
            device.wait(Until.gone(By.text(ALARM_TITLE)), TIMEOUT_MS)
            return
        }

        device.openNotification()
        clickTextIfVisible(DISMISS_LABEL, 1_000L)
        device.pressBack()
        device.wait(Until.gone(By.text(ALARM_TITLE)), TIMEOUT_MS)
    }

    fun waitForRealTime(durationMs: Long) {
        SystemClock.sleep(durationMs)
        instrumentation.waitForIdleSync()
    }

    private fun findAlarmToggleButton() =
        device.findObject(By.desc(ENABLE_BUTTON))
            ?: device.findObject(By.desc(DISABLE_BUTTON))
            ?: error("Alarm toggle button was not visible")

    private fun waitForMainScreen() {
        val deadline = SystemClock.uptimeMillis() + TIMEOUT_MS
        while (SystemClock.uptimeMillis() < deadline) {
            if (isMainScreenVisible()) {
                return
            }
            SystemClock.sleep(200)
        }
        error("Main settings screen was not ready")
    }

    private fun isMainScreenVisible(): Boolean =
        device.hasObject(By.desc(ENABLED_LABEL)) ||
            device.hasObject(By.desc(DISABLED_LABEL))

    private fun isAlarmActiveOnScreen(): Boolean =
        device.hasObject(By.text(ALARM_TITLE)) && device.hasObject(By.text(DISMISS_LABEL))

    private fun isAlarmNotificationVisible(): Boolean {
        device.openNotification()
        val titleObject = device.wait(Until.findObject(By.text(ALARM_TITLE)), 1_000L)
        if (titleObject == null) {
            val dismissOnNotification = device.hasObject(By.text(DISMISS_LABEL))
            device.pressBack()
            return dismissOnNotification
        }

        runCatching {
            titleObject.click()
        }
        if (device.wait(Until.hasObject(By.text(DISMISS_LABEL)), TIMEOUT_MS)) {
            return true
        }

        device.pressBack()
        device.openNotification()
        val dismissOnNotification = device.findObject(By.text(DISMISS_LABEL)) != null
        device.pressBack()
        return dismissOnNotification
    }

    private fun clickTextIfVisible(text: String, timeoutMs: Long): Boolean {
        val deadline = SystemClock.uptimeMillis() + timeoutMs
        while (SystemClock.uptimeMillis() < deadline) {
            val textObject = device.wait(Until.findObject(By.text(text)), 200L)
            if (textObject != null) {
                try {
                    textObject.click()
                    return true
                } catch (exception: StaleObjectException) {
                    SystemClock.sleep(100)
                }
            }
        }
        return false
    }
}
