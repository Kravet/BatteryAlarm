package com.example.batteryalarm

import android.content.Intent
import android.os.Build
import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.example.batteryalarm.alarm.BatteryLowAlarmHandler
import java.io.File
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TestAlarmE2eTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val device = UiDevice.getInstance(instrumentation)

    @Before
    fun setUp() {
        wakeAndUnlockDevice()
        grantNotificationPermission()
        grantFullScreenIntentPermission()
        resetAppSettings()
        dismissAlarmUiIfVisible()
    }

    @After
    fun tearDown() {
        wakeAndUnlockDevice()
        dismissAlarmUiIfVisible()
        runShell("am kill $PACKAGE_NAME")
        device.pressHome()
    }

    @Test
    fun test_alarm_foreground_shows_alarm_after_delay() {
        launchApp()
        enableBatteryAlarm()
        tapTestAlarmButton()

        waitForRealTime(BatteryLowAlarmHandler.TEST_ALARM_DELAY_MS + 2_000L)

        assertAlarmIsActive()
        dismissAlarmUiIfVisible()
        assertAlarmUiNotVisible()
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

    private fun tapTestAlarmButton() {
        val testButton = device.wait(
            Until.findObject(By.text("Test")),
            TIMEOUT_MS,
        ) ?: error("Test alarm button was not visible")
        testButton.click()
    }

    private fun assertAlarmIsActive() {
        if (isAlarmNotificationVisible()) {
            return
        }

        assertTrue(
            "Alarm UI was not visible via notification or full-screen overlay",
            device.wait(Until.hasObject(By.text(ALARM_TITLE)), TIMEOUT_MS),
        )
        assertTrue(
            "Dismiss action was not visible",
            device.wait(Until.hasObject(By.text(DISMISS_LABEL)), TIMEOUT_MS),
        )
    }

    private fun isAlarmNotificationVisible(): Boolean {
        device.openNotification()
        val titleVisible = device.wait(Until.hasObject(By.text(ALARM_TITLE)), TIMEOUT_MS)
        val dismissVisible = device.wait(Until.findObject(By.text(DISMISS_LABEL)), TIMEOUT_MS) != null
        device.pressBack()
        return titleVisible && dismissVisible
    }

    private fun assertAlarmUiNotVisible() {
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

    private fun resetAppSettings() {
        File(context.filesDir, "datastore/alarm_settings.preferences_pb").delete()
    }

    private fun grantNotificationPermission() {
        runShell("pm grant $PACKAGE_NAME android.permission.POST_NOTIFICATIONS")
    }

    private fun grantFullScreenIntentPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            runShell("appops set $PACKAGE_NAME USE_FULL_SCREEN_INTENT allow")
        }
    }

    private fun wakeAndUnlockDevice() {
        runShell("input keyevent KEYCODE_WAKEUP")
        runShell("wm dismiss-keyguard")
    }

    private fun dismissAlarmUiIfVisible() {
        val dismissOnScreen = device.wait(
            Until.findObject(By.text(DISMISS_LABEL)),
            1_000L,
        )
        if (dismissOnScreen != null) {
            dismissOnScreen.click()
            device.wait(Until.gone(By.text(ALARM_TITLE)), TIMEOUT_MS)
            return
        }

        device.openNotification()
        val dismissOnNotification = device.wait(
            Until.findObject(By.text(DISMISS_LABEL)),
            1_000L,
        )
        dismissOnNotification?.click()
        device.pressBack()
        device.wait(Until.gone(By.text(ALARM_TITLE)), TIMEOUT_MS)
    }

    private fun waitForRealTime(durationMs: Long) {
        SystemClock.sleep(durationMs)
        instrumentation.waitForIdleSync()
    }

    private fun runShell(command: String): String = device.executeShellCommand(command)

    private companion object {
        const val PACKAGE_NAME = "com.example.batteryalarm"
        const val ALARM_TITLE = "Low battery alarm"
        const val DISMISS_LABEL = "Dismiss"
        const val TIMEOUT_MS = 5_000L
    }
}
