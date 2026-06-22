package com.example.batteryalarm

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

/**
 * E2E for alarm monitoring persistence across reboot and process death.
 *
 * ```
 * | alarm   | event                         | battery low | alarm UI active |
 * |---------|-------------------------------|-------------|-----------------|
 * | enabled | device reboot                 | yes         | yes             |
 * | enabled | process + monitoring FS killed| yes         | yes             |
 * ```
 */
@RunWith(AndroidJUnit4::class)
class AlarmPersistenceE2eTest {
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
        actions.dismissAlarmUiIfVisible()
    }

    @After
    fun tearDown() {
        actions.wakeAndUnlock()
        actions.runShell("dumpsys battery reset")
        actions.resetAppSettings()
        actions.dismissAlarmUiIfVisible()
        actions.killAppProcess()
        device.pressHome()
    }

    /**
     * Requires a full device reboot and instrumentation reconnect; kept ignored for CI.
     */
    @Test
    @Ignore("Requires device reboot and instrumentation reconnect")
    fun enabled_after_reboot_reacts_to_battery_low() {
        actions.launchApp()
        actions.enableBatteryAlarm()
        rebootDeviceAndWaitForBootComplete()

        triggerBatteryLowWithoutOpeningApp()

        actions.assertMonitoringServiceRunning()
        actions.wakeAndUnlock()
        actions.assertAlarmIsActive()
    }

    @Test
    @Ignore("Requires device reboot and instrumentation reconnect")
    fun enabled_after_process_kill_monitoring_restarts_and_reacts_to_battery_low() {
        actions.launchApp()
        actions.enableBatteryAlarm()
        actions.assertMonitoringServiceRunning()

        actions.backgroundApp()
        actions.killAppProcess()
        actions.waitForMonitoringServiceRestart()

        triggerBatteryLowWithoutOpeningApp()
        actions.waitForRealTime(3_000L)

        actions.assertMonitoringServiceRunning()
        actions.wakeAndUnlock()
        actions.assertAlarmIsActive()
    }

    private fun triggerBatteryLowWithoutOpeningApp() {
        actions.triggerSystemBatteryLow()
    }

    private fun rebootDeviceAndWaitForBootComplete() {
        actions.runShell("reboot")
        error(
            "Device reboot initiated; reconnect instrumentation and re-run this test manually",
        )
    }
}
