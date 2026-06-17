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

/**
 * E2E matrix for low-battery alarm UI (alarm enabled × app lifecycle × battery low).
 *
 * ```
 * | alarm   | lifecycle     | battery low | alarm screen |
 * |---------|---------------|-------------|--------------|
 * | disabled| foreground    | yes         | no           |
 * | enabled | foreground    | yes         | yes          |
 * | enabled | background    | yes         | yes          |
 * | enabled | locked+doze   | yes         | yes          |
 * ```
 *
 * Not covered here (same domain path or trivial):
 * - disabled × background/locked — [DefaultAlarmController] returns Disabled regardless of lifecycle
 * - enabled/disabled × * × no low battery — no broadcast, nothing to assert
 */
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

    // --- when alarm disabled ---

    @Test
    fun disabled_foreground() = runScenario(SCENARIOS.disabledForeground)

    // --- when alarm enabled ---

    @Test
    fun enabled_foreground() = runScenario(SCENARIOS.enabledForeground)

    @Test
    fun enabled_background() = runScenario(SCENARIOS.enabledBackground)

    @Test
    fun enabled_locked_doze() = runScenario(SCENARIOS.enabledLockedDoze)

    private fun runScenario(scenario: LowBatteryScenario) {
        launchApp()
        applyAlarmSetting(scenario.alarm)
        applyAppLifecycle(scenario.lifecycle)

        triggerSystemBatteryLow()

        if (scenario.lifecycle == AppLifecycle.LOCKED_IN_DOZE) {
            runShell("cmd deviceidle unforce")
            wakeAndUnlockDevice()
            launchApp()
        }

        if (scenario.expectAlarmScreen) {
            assertAlarmScreenVisible()
        } else {
            assertAlarmScreenNotVisible()
        }
    }

    private fun applyAlarmSetting(alarm: AlarmSetting) {
        when (alarm) {
            AlarmSetting.ENABLED -> enableBatteryAlarm()
            AlarmSetting.DISABLED -> disableBatteryAlarm()
        }
    }

    private fun applyAppLifecycle(lifecycle: AppLifecycle) {
        when (lifecycle) {
            AppLifecycle.FOREGROUND -> Unit
            AppLifecycle.BACKGROUND -> {
                device.pressHome()
                waitUntilLauncherVisible()
            }
            AppLifecycle.LOCKED_IN_DOZE -> {
                device.pressHome()
                waitUntilLauncherVisible()
                runShell("cmd deviceidle force-idle")
                runShell("input keyevent KEYCODE_SLEEP")
            }
        }
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

        val SCENARIOS = LowBatteryScenarios
    }
}

private enum class AlarmSetting {
    ENABLED,
    DISABLED,
}

private enum class AppLifecycle {
    FOREGROUND,
    BACKGROUND,
    LOCKED_IN_DOZE,
}

private data class LowBatteryScenario(
    val id: String,
    val alarm: AlarmSetting,
    val lifecycle: AppLifecycle,
    val expectAlarmScreen: Boolean,
)

private object LowBatteryScenarios {
    val disabledForeground = LowBatteryScenario(
        id = "disabled_foreground",
        alarm = AlarmSetting.DISABLED,
        lifecycle = AppLifecycle.FOREGROUND,
        expectAlarmScreen = false,
    )

    val enabledForeground = LowBatteryScenario(
        id = "enabled_foreground",
        alarm = AlarmSetting.ENABLED,
        lifecycle = AppLifecycle.FOREGROUND,
        expectAlarmScreen = true,
    )

    val enabledBackground = LowBatteryScenario(
        id = "enabled_background",
        alarm = AlarmSetting.ENABLED,
        lifecycle = AppLifecycle.BACKGROUND,
        expectAlarmScreen = true,
    )

    val enabledLockedDoze = LowBatteryScenario(
        id = "enabled_locked_doze",
        alarm = AlarmSetting.ENABLED,
        lifecycle = AppLifecycle.LOCKED_IN_DOZE,
        expectAlarmScreen = true,
    )
}
