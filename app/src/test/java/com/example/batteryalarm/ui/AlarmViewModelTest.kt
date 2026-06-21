package com.example.batteryalarm.ui

import com.example.batteryalarm.domain.AlarmController
import com.example.batteryalarm.domain.AlarmStartReason
import com.example.batteryalarm.domain.AlarmStartResult
import com.example.batteryalarm.domain.AlarmState
import com.example.batteryalarm.domain.AlarmStopReason
import com.example.batteryalarm.domain.AlarmStopResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AlarmViewModelTest {
    @Test
    fun `is alarm active returns true when alarm is active`() {
        val alarmController = AlarmViewModelFakeAlarmController(
            initialState = AlarmState.Active(AlarmStartReason.SystemLowBattery),
        )
        val viewModel = AlarmViewModel(alarmController = alarmController)

        assertTrue(viewModel.isAlarmActive())
    }

    @Test
    fun `is alarm active returns false when alarm is idle`() {
        val viewModel = AlarmViewModel(alarmController = AlarmViewModelFakeAlarmController())

        assertFalse(viewModel.isAlarmActive())
    }

    @Test
    fun `dismiss alarm stops outputs`() {
        val alarmController = AlarmViewModelFakeAlarmController(
            initialState = AlarmState.Active(AlarmStartReason.TestAlarmFlow),
        )
        val viewModel = AlarmViewModel(alarmController = alarmController)

        viewModel.onDismissAlarmClick()

        assertEquals(listOf(AlarmStopReason.UserDismissed), alarmController.stoppedReasons)
        assertFalse(viewModel.isAlarmActive())
    }
}

private class AlarmViewModelFakeAlarmController(
    initialState: AlarmState = AlarmState.Idle,
) : AlarmController {
    override var state: AlarmState = initialState
        private set

    val stoppedReasons = mutableListOf<AlarmStopReason>()

    override suspend fun startAlarm(reason: AlarmStartReason): AlarmStartResult {
        error("Not used in AlarmViewModelTest")
    }

    override fun stopAlarm(reason: AlarmStopReason): AlarmStopResult {
        stoppedReasons += reason

        val activeState = state as? AlarmState.Active ?: return AlarmStopResult.AlreadyIdle
        state = AlarmState.Idle
        return AlarmStopResult.Stopped(
            previousState = activeState,
            reason = reason,
        )
    }
}
