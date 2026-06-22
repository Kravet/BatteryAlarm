package com.example.batteryalarm

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TestAlarmE2eTest {
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
        actions.prepareCleanTestState()
        actions.establishDisabledBaseline()
    }

    @After
    fun tearDown() {
        runCatching {
            actions.wakeAndUnlock()
            actions.dismissAlarmUiIfVisible()
        }
        device.pressHome()
    }

    @Test
    fun test_alarm_foreground_shows_alarm_after_delay() {
        actions.launchApp()
        actions.enableBatteryAlarm()
        E2eAlarmLog.awaitMonitoringStarted(actions::runShell)

        actions.tapTestAlarmButton()
        E2eAlarmLog.awaitTestAlarmStarted(actions::runShell)

        actions.wakeAndUnlock()
        actions.assertAlarmIsActive()
        actions.dismissAlarmUiIfVisible()
        actions.assertAlarmUiNotVisible()
    }
}
