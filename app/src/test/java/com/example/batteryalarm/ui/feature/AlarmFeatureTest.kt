package com.example.batteryalarm.ui.feature

import com.example.batteryalarm.alarm.BatteryLowAlarmHandler
import com.example.batteryalarm.analytics.FakeAppAnalytics
import com.example.batteryalarm.domain.AlarmStartReason
import com.example.batteryalarm.domain.AlarmState
import com.example.batteryalarm.ui.MainDispatcherRule
import com.example.batteryalarm.ui.MainSideEffect
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AlarmFeatureTest {
    @get:Rule val dispatcherRule = MainDispatcherRule()

    private fun TestScope.feature(
        settings: FeatureAlarmSettingsRepository = FeatureAlarmSettingsRepository(),
        controller: FeatureAlarmController = FeatureAlarmController(),
        analytics: FakeAppAnalytics = FakeAppAnalytics(),
    ): AlarmFeature = AlarmFeature(
        settingsRepository = settings,
        batteryLowAlarmHandler = BatteryLowAlarmHandler(controller, this),
        alarmController = controller,
        analytics = analytics,
    )

    @Test fun `observing settings uses persisted enabled flag`() = runTest {
        val feature = feature(settings = FeatureAlarmSettingsRepository(enabled = true))
        feature.observe(backgroundScope)
        runCurrent()
        assertTrue(feature.state.value.isEnabled)
    }

    @Test fun `toggle uses persisted result and records analytics`() = runTest {
        val settings = FeatureAlarmSettingsRepository(storedOverride = false)
        val analytics = FakeAppAnalytics()
        val feature = feature(settings = settings, analytics = analytics)
        feature.observe(backgroundScope)
        runCurrent()

        feature.toggle(this)
        advanceUntilIdle()

        assertFalse(feature.state.value.isEnabled)
        assertEquals(listOf(false), analytics.alarmToggled)
    }

    @Test fun `toggle failure preserves state and emits failure effect`() = runTest {
        val settings = FeatureAlarmSettingsRepository(failure = IllegalStateException("Disk failed"))
        val analytics = FakeAppAnalytics()
        val feature = feature(settings = settings, analytics = analytics)
        val effects = mutableListOf<MainSideEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            feature.sideEffects.toCollection(effects)
        }
        feature.observe(backgroundScope)
        runCurrent()

        feature.toggle(this)
        advanceUntilIdle()

        assertFalse(feature.state.value.isEnabled)
        assertTrue(analytics.alarmToggled.isEmpty())
        assertEquals(listOf(MainSideEffect.ShowAlarmSettingsChangeFailed), effects)
    }

    @Test fun `notification permission denial logs analytics`() = runTest {
        val analytics = FakeAppAnalytics()
        feature(analytics = analytics).onNotificationPermissionDenied()
        assertEquals(1, analytics.notificationPermissionDeniedCount)
    }

    @Test fun `alarm activity reflects controller state`() = runTest {
        val active = feature(controller = FeatureAlarmController(
            AlarmState.Active(AlarmStartReason.SystemLowBattery),
        ))
        val idle = feature()
        assertTrue(active.isAlarmActive())
        assertFalse(idle.isAlarmActive())
    }

    @Test fun `test alarm counts down then triggers shared controller once`() = runTest {
        val controller = FeatureAlarmController()
        val analytics = FakeAppAnalytics()
        val feature = feature(controller = controller, analytics = analytics)

        feature.startTestAlarm(this)
        assertTrue(feature.state.value.isTestAlarmPending)
        assertEquals(4, feature.state.value.testAlarmSecondsRemaining)
        assertTrue(controller.startedReasons.isEmpty())

        advanceTimeBy(BatteryLowAlarmHandler.TEST_ALARM_TICK_MS)
        runCurrent()
        assertEquals(3, feature.state.value.testAlarmSecondsRemaining)

        advanceUntilIdle()
        assertFalse(feature.state.value.isTestAlarmPending)
        assertEquals(null, feature.state.value.testAlarmSecondsRemaining)
        assertEquals(listOf(AlarmStartReason.TestAlarmFlow), controller.startedReasons)
        assertEquals(1, analytics.testAlarmStartedCount)
    }

    @Test fun `second test alarm click during countdown is ignored`() = runTest {
        val controller = FeatureAlarmController()
        val analytics = FakeAppAnalytics()
        val feature = feature(controller = controller, analytics = analytics)

        feature.startTestAlarm(this)
        feature.startTestAlarm(this)
        advanceUntilIdle()

        assertEquals(1, analytics.testAlarmStartedCount)
        assertEquals(listOf(AlarmStartReason.TestAlarmFlow), controller.startedReasons)
    }

    @Test fun `test alarm failure resets countdown and emits failure effect`() = runTest {
        val analytics = FakeAppAnalytics()
        val feature = feature(
            controller = FeatureAlarmController(startFailure = IllegalStateException("Output failed")),
            analytics = analytics,
        )
        val effects = mutableListOf<MainSideEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            feature.sideEffects.toCollection(effects)
        }

        feature.startTestAlarm(this)
        advanceUntilIdle()

        assertFalse(feature.state.value.isTestAlarmPending)
        assertEquals(null, feature.state.value.testAlarmSecondsRemaining)
        assertEquals(1, analytics.testAlarmFailedCount)
        assertEquals(listOf(MainSideEffect.ShowTestAlarmStartFailed), effects)
    }

    @Test fun `toggling settings during test countdown preserves countdown`() = runTest {
        val settings = FeatureAlarmSettingsRepository()
        val feature = feature(settings = settings)
        feature.observe(backgroundScope)
        runCurrent()

        feature.startTestAlarm(this)
        feature.toggle(this)
        runCurrent()

        assertTrue(feature.state.value.isEnabled)
        assertTrue(feature.state.value.isTestAlarmPending)
        assertEquals(4, feature.state.value.testAlarmSecondsRemaining)
    }
}
