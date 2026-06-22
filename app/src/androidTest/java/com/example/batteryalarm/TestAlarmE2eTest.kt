package com.example.batteryalarm

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.example.batteryalarm.alarm.BatteryLowAlarmHandler
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
        actions.wakeAndUnlock()
        actions.dismissAlarmUiIfVisible()
        device.pressHome()
    }

    @Test
    fun test_alarm_foreground_shows_alarm_after_delay() {
        actions.launchApp()
        actions.enableBatteryAlarm()
        actions.tapTestAlarmButton()

        actions.waitForRealTime(BatteryLowAlarmHandler.TEST_ALARM_DELAY_MS + 2_000L)

        actions.assertAlarmIsActive()
        actions.dismissAlarmUiIfVisible()
        actions.assertAlarmUiNotVisible()
    }
}
