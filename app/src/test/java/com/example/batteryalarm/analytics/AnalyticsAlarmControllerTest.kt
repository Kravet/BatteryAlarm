package com.example.batteryalarm.analytics

import com.example.batteryalarm.domain.AlarmController
import com.example.batteryalarm.domain.AlarmStartReason
import com.example.batteryalarm.domain.AlarmStartResult
import com.example.batteryalarm.domain.AlarmState
import com.example.batteryalarm.domain.AlarmStopReason
import com.example.batteryalarm.domain.AlarmStopResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
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
        )

        controller.stopAlarm(AlarmStopReason.UserDismissed)

        assertEquals(1, analytics.alarmStopped.size)
        assertEquals("user_dismissed", analytics.alarmStopped.single().first)
    }

    private class FakeDelegateController(
        private val startResult: AlarmStartResult = AlarmStartResult.Disabled,
        private val stopResult: AlarmStopResult = AlarmStopResult.AlreadyIdle,
    ) : AlarmController {
        override var state: AlarmState = AlarmState.Idle

        override suspend fun startAlarm(reason: AlarmStartReason): AlarmStartResult = startResult

        override fun stopAlarm(reason: AlarmStopReason): AlarmStopResult = stopResult
    }
}
