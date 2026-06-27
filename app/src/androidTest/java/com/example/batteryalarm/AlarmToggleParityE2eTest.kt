package com.example.batteryalarm

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * E2E for alarm toggle click parity: after rapid taps, persisted state, UI, and
 * monitoring service must all match the expected result (baseline XOR odd tap count).
 */
@RunWith(AndroidJUnit4::class)
class AlarmToggleParityE2eTest {
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
            actions.launchApp()
            actions.disableBatteryAlarm()
        }
        device.pressHome()
    }

    @Test
    fun rapid_toggles_eventual_state_matches_click_parity_without_divergence() {
        actions.launchApp()
        actions.assertAlarmEnabledConsistent(expectedEnabled = false)

        var expectedEnabled = false
        val rapidTapCounts = listOf(1, 2, 7, 20, 31, 50)

        for (tapCount in rapidTapCounts) {
            actions.tapAlarmToggleRapidly(tapCount)
            if (tapCount % 2 == 1) {
                expectedEnabled = !expectedEnabled
            }
            actions.waitForAlarmToggleSettled(expectedEnabled)
            actions.assertAlarmEnabledConsistent(expectedEnabled)
        }
    }
}
