package com.example.batteryalarm

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import java.io.File
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LowBatteryE2eTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val device = UiDevice.getInstance(instrumentation)

    @Before
    fun setUp() {
        wakeAndUnlockDevice()
        runShell("cmd deviceidle unforce")
        resetBatteryState()
        resetAppSettings()
    }

    @After
    fun tearDown() {
        wakeAndUnlockDevice()
        runShell("cmd deviceidle unforce")
        runShell("dumpsys battery reset")
        resetAppSettings()
        runShell("am kill $PACKAGE_NAME")
        device.pressHome()
    }

    @Test
    fun whenAlarmIsDisabledAndBatteryLowReceived_thenDoNotShowAlarm() {
        launchApp()
        disableBatteryAlarm()

        drainBatteryWithoutExpectingAlarm()
    }

    @Test
    fun whenAppIsOpenedAndBatteryLowReceived_thenShowAlarm() {
        launchApp()
        enableBatteryAlarm()

        drainBatteryUntilAlarmIsVisible()
    }

    @Test
    fun whenAppIsClosedAndBatteryLowReceived_thenShowAlarm() {
        launchApp()
        enableBatteryAlarm()
        device.pressHome()
        waitUntilLauncherVisible()

        drainBatteryUntilAlarmIsVisible()
    }

    @Test
    fun whenAppIsLockedAndDozeAndBatteryLowReceived_thenShowAlarm() {
        launchApp()
        enableBatteryAlarm()
        device.pressHome()
        waitUntilLauncherVisible()

        runShell("cmd deviceidle force-idle")
        runShell("input keyevent KEYCODE_SLEEP")

        triggerSystemBatteryLow()

        runShell("cmd deviceidle unforce")
        wakeAndUnlockDevice()
        launchApp()
        assertAlarmScreenVisible()
    }

    private fun launchApp() {
        val intent = context.packageManager.getLaunchIntentForPackage(PACKAGE_NAME)
            ?: error("Could not find launch intent for $PACKAGE_NAME")
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        assertTrue(
            "App did not open",
            device.wait(Until.hasObject(By.pkg(PACKAGE_NAME).depth(0)), TIMEOUT_MS),
        )
    }

    private fun enableBatteryAlarm() {
        dismissAlarmScreenIfVisible()

        if (device.wait(Until.hasObject(By.text("Battery alarm is enabled")), TIMEOUT_MS)) {
            return
        }

        val enableButton = device.wait(
            Until.findObject(By.text("Enable battery alarm")),
            TIMEOUT_MS,
        ) ?: error("Enable battery alarm button was not visible")

        enableButton.click()

        assertTrue(
            "Battery alarm setting did not become enabled",
            device.wait(Until.hasObject(By.text("Battery alarm is enabled")), TIMEOUT_MS),
        )
    }

    private fun disableBatteryAlarm() {
        dismissAlarmScreenIfVisible()

        if (device.wait(Until.hasObject(By.text("Battery alarm is disabled")), TIMEOUT_MS)) {
            return
        }

        val disableButton = device.wait(
            Until.findObject(By.text("Disable battery alarm")),
            TIMEOUT_MS,
        ) ?: error("Disable battery alarm button was not visible")

        disableButton.click()

        assertTrue(
            "Battery alarm setting did not become disabled",
            device.wait(Until.hasObject(By.text("Battery alarm is disabled")), TIMEOUT_MS),
        )
    }

    private fun drainBatteryUntilAlarmIsVisible() {
        triggerSystemBatteryLow()
        assertAlarmScreenVisible()
    }

    private fun drainBatteryWithoutExpectingAlarm() {
        triggerSystemBatteryLow()
        assertAlarmScreenNotVisible()
    }

    private fun triggerSystemBatteryLow() {
        runShell("dumpsys battery set level 50")
        runShell("dumpsys battery set level 10")
    }

    private fun assertAlarmScreenVisible() {
        assertTrue(
            "Low battery alarm screen was not visible",
            device.wait(Until.hasObject(By.text("Low battery alarm")), TIMEOUT_MS),
        )
    }

    private fun assertAlarmScreenNotVisible() {
        assertFalse(
            "Low battery alarm screen was visible but should not be",
            device.wait(Until.hasObject(By.text("Low battery alarm")), TIMEOUT_MS),
        )
    }

    private fun resetBatteryState() {
        runShell("dumpsys battery reset")
        runShell("dumpsys battery unplug")
        runShell("dumpsys battery set status 3")
        runShell("dumpsys battery set level 50")
    }

    private fun resetAppSettings() {
        File(context.filesDir, "datastore/alarm_settings.preferences_pb").delete()
    }

    private fun wakeAndUnlockDevice() {
        runShell("input keyevent KEYCODE_WAKEUP")
        runShell("wm dismiss-keyguard")
    }

    private fun dismissAlarmScreenIfVisible() {
        val stopButton = device.wait(
            Until.findObject(By.text("Stop")),
            1_000L,
        ) ?: return

        stopButton.click()
        device.wait(Until.gone(By.text("Low battery alarm")), TIMEOUT_MS)
    }

    private fun waitUntilLauncherVisible() {
        device.wait(Until.gone(By.pkg(PACKAGE_NAME)), TIMEOUT_MS)
    }

    private fun runShell(command: String): String = device.executeShellCommand(command)

    private companion object {
        const val PACKAGE_NAME = "com.example.batteryalarm"
        const val TIMEOUT_MS = 5_000L
    }
}
