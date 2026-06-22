package com.example.batteryalarm

import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.example.batteryalarm.E2eTestConstants.ALARM_TITLE
import com.example.batteryalarm.E2eTestConstants.DISABLE_BUTTON
import com.example.batteryalarm.E2eTestConstants.DISABLED_LABEL
import com.example.batteryalarm.E2eTestConstants.DISMISS_LABEL
import com.example.batteryalarm.E2eTestConstants.ENABLE_BUTTON
import com.example.batteryalarm.E2eTestConstants.ENABLED_LABEL
import com.example.batteryalarm.E2eTestConstants.TIMEOUT_MS
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters

/**
 * E2E matrix for low-battery alarm UI (alarm enabled × app lifecycle × battery low).
 */
@RunWith(AndroidJUnit4::class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class LowBatteryE2eTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val device = UiDevice.getInstance(instrumentation)
    private lateinit var actions: E2eTestActions

    @Before
    fun setUp() {
        actions = E2eTestActions(
            context = instrumentation.targetContext,
            device = device,
            instrumentation = instrumentation,
        )
        actions.prepareCleanTestState(resetBattery = true)
        actions.runShell("cmd deviceidle unforce")
    }

    @After
    fun tearDown() {
        runCatching {
            actions.wakeAndUnlock()
            actions.runShell("cmd deviceidle unforce")
            actions.dismissAlarmUiIfVisible()
            actions.runShell("dumpsys battery reset")
            openMainSettings()
            tapDisableIfNeeded()
        }
        device.pressHome()
    }

    @Test
    fun disabled_foreground() = runLowBatteryScenario(SCENARIOS.disabledForeground)

    @Test
    fun enabled_foreground() = runLowBatteryScenario(SCENARIOS.enabledForeground)

    @Test
    fun enabled_background() = runLowBatteryScenario(SCENARIOS.enabledBackground)

    @Test
    fun enabled_locked_doze() = runLowBatteryScenario(SCENARIOS.enabledLockedDoze)

    private fun runLowBatteryScenario(scenario: LowBatteryScenario) {
        openMainSettings()

        when (scenario.alarm) {
            AlarmSetting.ENABLED -> {
                tapEnableIfNeeded()
                LowBatteryE2eLog.awaitMonitoringStarted(actions::runShell)
            }
            AlarmSetting.DISABLED -> tapDisableIfNeeded()
        }

        when (scenario.lifecycle) {
            AppLifecycle.FOREGROUND -> Unit
            AppLifecycle.BACKGROUND -> actions.backgroundApp()
            AppLifecycle.LOCKED_IN_DOZE -> enterDozeWithoutSleep()
        }

        triggerSystemBatteryLow()

        if (scenario.lifecycle == AppLifecycle.LOCKED_IN_DOZE) {
            actions.exitDozeAndWake()
        } else if (scenario.expectAlarmUi) {
            actions.wakeAndUnlock()
        }

        if (scenario.expectAlarmUi) {
            LowBatteryE2eLog.awaitBatteryLowHandled(actions::runShell)
            LowBatteryE2eLog.awaitAlarmStarted(actions::runShell)
            assertAlarmIsVisible()
        } else {
            assertAlarmIsNotVisible()
        }
    }

    private fun openMainSettings() {
        actions.dismissAlarmUiIfVisible()
        actions.launchApp()
        instrumentation.waitForIdleSync()
        SystemClock.sleep(1_000)
        val deadline = SystemClock.uptimeMillis() + MAIN_SCREEN_TIMEOUT_MS
        while (SystemClock.uptimeMillis() < deadline) {
            if (isMainSettingsVisible()) {
                return
            }
            SystemClock.sleep(250)
        }
        error("Main settings screen was not ready")
    }

    private fun tapEnableIfNeeded() {
        if (device.hasObject(By.text(ENABLED_LABEL))) {
            actions.waitForMonitoringService()
            return
        }

        device.wait(Until.findObject(By.text(ENABLE_BUTTON)), TIMEOUT_MS)?.click()
            ?: error("Enable battery alarm button was not visible")

        assertTrue(
            "Battery alarm setting did not become enabled",
            device.wait(Until.hasObject(By.text(ENABLED_LABEL)), TIMEOUT_MS),
        )
        actions.waitForMonitoringService()
    }

    private fun tapDisableIfNeeded() {
        if (device.hasObject(By.text(DISABLED_LABEL))) {
            return
        }

        device.wait(Until.findObject(By.text(DISABLE_BUTTON)), TIMEOUT_MS)?.click()
            ?: error("Disable battery alarm button was not visible")

        assertTrue(
            "Battery alarm setting did not become disabled",
            device.wait(Until.hasObject(By.text(DISABLED_LABEL)), TIMEOUT_MS),
        )
        actions.waitForMonitoringServiceStopped()
    }

    private fun isMainSettingsVisible(): Boolean =
        device.hasObject(By.text(ENABLED_LABEL)) ||
            device.hasObject(By.text(DISABLED_LABEL)) ||
            device.hasObject(By.text(ENABLE_BUTTON)) ||
            device.hasObject(By.text(DISABLE_BUTTON)) ||
            device.hasObject(By.textContains("Battery alarm"))

    private fun enterDozeWithoutSleep() {
        actions.backgroundApp()
        actions.runShell("cmd deviceidle force-idle")
    }

    private fun triggerSystemBatteryLow() {
        actions.runShell("dumpsys battery unplug")
        actions.runShell("dumpsys battery set status 3")
        actions.runShell("dumpsys battery set level 20")
        SystemClock.sleep(500)
        actions.runShell("dumpsys battery set level 5")
    }

    private fun assertAlarmIsVisible() {
        val deadline = SystemClock.uptimeMillis() + ALARM_ASSERT_TIMEOUT_MS
        while (SystemClock.uptimeMillis() < deadline) {
            if (isAlarmVisibleOnScreen() || isAlarmVisibleInNotificationShade()) {
                return
            }
            SystemClock.sleep(500)
        }

        assertTrue(
            "Alarm UI was not visible via notification or full-screen overlay",
            isAlarmVisibleOnScreen() || isAlarmVisibleInNotificationShade(),
        )
    }

    private fun assertAlarmIsNotVisible() {
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

    private fun isAlarmVisibleOnScreen(): Boolean =
        device.hasObject(By.text(ALARM_TITLE)) && device.hasObject(By.text(DISMISS_LABEL))

    private fun isAlarmVisibleInNotificationShade(): Boolean {
        device.openNotification()
        val titleObject = device.wait(Until.findObject(By.text(ALARM_TITLE)), TIMEOUT_MS)
        if (titleObject == null) {
            device.pressBack()
            return false
        }

        titleObject.click()
        if (device.wait(Until.hasObject(By.text(DISMISS_LABEL)), TIMEOUT_MS)) {
            return true
        }

        device.pressBack()
        device.openNotification()
        val dismissOnNotification = device.findObject(By.text(DISMISS_LABEL)) != null
        device.pressBack()
        return dismissOnNotification
    }

    private companion object {
        const val MAIN_SCREEN_TIMEOUT_MS = 10_000L
        const val ALARM_ASSERT_TIMEOUT_MS = 15_000L
        val SCENARIOS = LowBatteryScenarios
    }
}

private object LowBatteryE2eLog {
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

    fun awaitAlarmStarted(runShell: (String) -> String) {
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
    val alarm: AlarmSetting,
    val lifecycle: AppLifecycle,
    val expectAlarmUi: Boolean,
)

private object LowBatteryScenarios {
    val disabledForeground = LowBatteryScenario(
        alarm = AlarmSetting.DISABLED,
        lifecycle = AppLifecycle.FOREGROUND,
        expectAlarmUi = false,
    )

    val enabledForeground = LowBatteryScenario(
        alarm = AlarmSetting.ENABLED,
        lifecycle = AppLifecycle.FOREGROUND,
        expectAlarmUi = true,
    )

    val enabledBackground = LowBatteryScenario(
        alarm = AlarmSetting.ENABLED,
        lifecycle = AppLifecycle.BACKGROUND,
        expectAlarmUi = true,
    )

    val enabledLockedDoze = LowBatteryScenario(
        alarm = AlarmSetting.ENABLED,
        lifecycle = AppLifecycle.LOCKED_IN_DOZE,
        expectAlarmUi = true,
    )
}
