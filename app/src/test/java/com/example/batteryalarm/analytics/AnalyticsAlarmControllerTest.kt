package com.example.batteryalarm.analytics

import com.example.batteryalarm.domain.AlarmController
import com.example.batteryalarm.domain.AlarmStartReason
import com.example.batteryalarm.domain.AlarmStartResult
import com.example.batteryalarm.domain.AlarmState
import com.example.batteryalarm.domain.AlarmStopReason
import com.example.batteryalarm.domain.AlarmStopResult
import java.lang.reflect.Proxy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class AnalyticsAlarmControllerTest {
    @Test
    fun `startAlarm Started logs alarm_started`() = runTest {
        val analytics = FakeAppAnalytics()
        val controller = AnalyticsAlarmController(
            delegate = FakeDelegateController(startResult = AlarmStartResult.Started(
                AlarmState.Active(AlarmStartReason.SystemLowBattery),
            )),
            analytics = analytics,
            errorReporter = FakeAppErrorReporter(),
        )

        controller.startAlarm(AlarmStartReason.SystemLowBattery)

        assertEquals(1, analytics.alarmStarted.size)
        assertEquals("system_low_battery", analytics.alarmStarted.single().first)
        assertTrue(analytics.alarmStarted.single().second in 0..23)
    }

    @Test
    fun `startAlarm Disabled logs alarm_start_blocked`() = runTest {
        val analytics = FakeAppAnalytics()
        val controller = AnalyticsAlarmController(
            delegate = FakeDelegateController(startResult = AlarmStartResult.Disabled),
            analytics = analytics,
            errorReporter = FakeAppErrorReporter(),
        )

        controller.startAlarm(AlarmStartReason.SystemLowBattery)

        assertEquals(listOf("disabled"), analytics.alarmStartBlocked)
    }

    @Test
    fun `stopAlarm Stopped logs alarm_stopped`() = runTest {
        val analytics = FakeAppAnalytics()
        val previous = AlarmState.Active(AlarmStartReason.TestAlarmFlow)
        val controller = AnalyticsAlarmController(
            delegate = FakeDelegateController(
                stopResult = AlarmStopResult.Stopped(previous, AlarmStopReason.UserDismissed),
            ),
            analytics = analytics,
            errorReporter = FakeAppErrorReporter(),
        )

        controller.stopAlarm(AlarmStopReason.UserDismissed)

        assertEquals(1, analytics.alarmStopped.size)
        assertEquals("user_dismissed", analytics.alarmStopped.single().first)
    }

    @Test
    fun `analytics failure does not replace a successful alarm start`() = runTest {
        val expected = AlarmStartResult.Started(
            AlarmState.Active(AlarmStartReason.SystemLowBattery),
        )
        val controller = AnalyticsAlarmController(
            delegate = FakeDelegateController(startResult = expected),
            analytics = failingAnalytics(),
            errorReporter = FakeAppErrorReporter(),
        )

        assertEquals(expected, controller.startAlarm(AlarmStartReason.SystemLowBattery))
    }

    @Test
    fun `analytics failure does not replace a successful alarm stop`() {
        val expected = AlarmStopResult.Stopped(
            previousState = AlarmState.Active(AlarmStartReason.SystemLowBattery),
            reason = AlarmStopReason.PowerConnected,
        )
        val controller = AnalyticsAlarmController(
            delegate = FakeDelegateController(stopResult = expected),
            analytics = failingAnalytics(),
            errorReporter = FakeAppErrorReporter(),
        )

        assertEquals(expected, controller.stopAlarm(AlarmStopReason.PowerConnected))
    }

    @Test
    fun `alarm start exception is reported and rethrown unchanged`() = runTest {
        val failure = IllegalStateException("start failed")
        val errorReporter = FakeAppErrorReporter()
        val controller = AnalyticsAlarmController(
            delegate = object : AlarmController {
                override val state: AlarmState = AlarmState.Idle
                override suspend fun startAlarm(reason: AlarmStartReason): AlarmStartResult {
                    throw failure
                }
                override fun stopAlarm(reason: AlarmStopReason): AlarmStopResult =
                    AlarmStopResult.AlreadyIdle
            },
            analytics = FakeAppAnalytics(),
            errorReporter = errorReporter,
        )

        try {
            controller.startAlarm(AlarmStartReason.SystemLowBattery)
            fail("Expected start exception")
        } catch (actual: IllegalStateException) {
            assertSame(failure, actual)
        }

        assertEquals(listOf("alarm_start" to failure), errorReporter.reported)
    }

    private fun failingAnalytics(): AppAnalytics = Proxy.newProxyInstance(
        AppAnalytics::class.java.classLoader,
        arrayOf(AppAnalytics::class.java),
    ) { _, _, _ -> throw IllegalStateException("analytics unavailable") } as AppAnalytics

    private class FakeDelegateController(
        private val startResult: AlarmStartResult = AlarmStartResult.Disabled,
        private val stopResult: AlarmStopResult = AlarmStopResult.AlreadyIdle,
    ) : AlarmController {
        override var state: AlarmState = AlarmState.Idle

        override suspend fun startAlarm(reason: AlarmStartReason): AlarmStartResult = startResult

        override fun stopAlarm(reason: AlarmStopReason): AlarmStopResult = stopResult
    }
}
