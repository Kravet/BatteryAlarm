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

    @Test
    fun opening_app_while_test_alarm_active_shows_alarm_screen_and_stop_exits_app() {
        startTestAlarmAndBackgroundApp()

        actions.launchAppExpectingAlarmScreen()
        actions.holdStopAlarmOnScreen()

        actions.assertAppNotInForeground()
        actions.assertAlarmUiNotVisible()
    }

    @Test
    fun pressing_back_from_app_launch_alarm_screen_exits_app_and_alarm_stays_active() {
        startTestAlarmAndBackgroundApp()

        actions.launchAppExpectingAlarmScreen()
        device.pressBack()

        actions.assertAppNotInForeground()

        actions.launchAppExpectingAlarmScreen()
        actions.holdStopAlarmOnScreen()
        actions.assertAppNotInForeground()
        actions.assertAlarmUiNotVisible()
    }

    private fun startTestAlarmAndBackgroundApp() {
        actions.launchApp()
        actions.enableBatteryAlarm()
        E2eAlarmLog.awaitMonitoringStarted(actions::runShell)

        actions.tapTestAlarmButton()
        E2eAlarmLog.awaitTestAlarmStarted(actions::runShell)

        actions.wakeAndUnlock()
        actions.assertAlarmIsActive()
        actions.backgroundApp()
    }
}
