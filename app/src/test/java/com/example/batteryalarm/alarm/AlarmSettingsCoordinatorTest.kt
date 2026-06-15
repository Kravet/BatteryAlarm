package com.example.batteryalarm.alarm

import com.example.batteryalarm.domain.AlarmController
import com.example.batteryalarm.domain.AlarmSettingsRepository
import com.example.batteryalarm.domain.AlarmStartReason
import com.example.batteryalarm.domain.AlarmStartResult
import com.example.batteryalarm.domain.AlarmState
import com.example.batteryalarm.domain.AlarmStopReason
import com.example.batteryalarm.domain.AlarmStopResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AlarmSettingsCoordinatorTest {
    @Test
    fun `initial enabled setting enables battery low receiver`() = runTest {
        val repository = FakeAlarmSettingsRepository(enabled = true)
        val registrar = FakeBatteryLowReceiverRegistrar()
        val alarmController = FakeAlarmController()
        val coordinator = AlarmSettingsCoordinator(
            alarmSettingsRepository = repository,
            batteryLowReceiverRegistrar = registrar,
            alarmController = alarmController,
            scope = CoroutineScope(backgroundScope.coroutineContext + UnconfinedTestDispatcher(testScheduler)),
        )

        coordinator.start()
        advanceUntilIdle()

        assertEquals(listOf(true), registrar.enabledValues)
        assertEquals(emptyList<AlarmStopReason>(), alarmController.stopReasons)
    }

    @Test
    fun `initial disabled setting disables receiver and stops alarm`() = runTest {
        val repository = FakeAlarmSettingsRepository(enabled = false)
        val registrar = FakeBatteryLowReceiverRegistrar()
        val alarmController = FakeAlarmController()
        val coordinator = AlarmSettingsCoordinator(
            alarmSettingsRepository = repository,
            batteryLowReceiverRegistrar = registrar,
            alarmController = alarmController,
            scope = CoroutineScope(backgroundScope.coroutineContext + UnconfinedTestDispatcher(testScheduler)),
        )

        coordinator.start()
        advanceUntilIdle()

        assertEquals(listOf(false), registrar.enabledValues)
        assertEquals(listOf(AlarmStopReason.SettingsDisabled), alarmController.stopReasons)
    }

    @Test
    fun `setting change from enabled to disabled disables receiver and stops alarm`() = runTest {
        val repository = FakeAlarmSettingsRepository(enabled = true)
        val registrar = FakeBatteryLowReceiverRegistrar()
        val alarmController = FakeAlarmController()
        val coordinator = AlarmSettingsCoordinator(
            alarmSettingsRepository = repository,
            batteryLowReceiverRegistrar = registrar,
            alarmController = alarmController,
            scope = CoroutineScope(backgroundScope.coroutineContext + UnconfinedTestDispatcher(testScheduler)),
        )

        coordinator.start()
        advanceUntilIdle()
        repository.setAlarmEnabled(false)
        advanceUntilIdle()

        assertEquals(listOf(true, false), registrar.enabledValues)
        assertEquals(listOf(AlarmStopReason.SettingsDisabled), alarmController.stopReasons)
    }

    @Test
    fun `start is idempotent and does not create duplicate collectors`() = runTest {
        val repository = FakeAlarmSettingsRepository(enabled = true)
        val registrar = FakeBatteryLowReceiverRegistrar()
        val alarmController = FakeAlarmController()
        val coordinator = AlarmSettingsCoordinator(
            alarmSettingsRepository = repository,
            batteryLowReceiverRegistrar = registrar,
            alarmController = alarmController,
            scope = CoroutineScope(backgroundScope.coroutineContext + UnconfinedTestDispatcher(testScheduler)),
        )

        coordinator.start()
        coordinator.start()
        advanceUntilIdle()
        repository.setAlarmEnabled(false)
        advanceUntilIdle()

        assertEquals(listOf(true, false), registrar.enabledValues)
        assertEquals(listOf(AlarmStopReason.SettingsDisabled), alarmController.stopReasons)
    }
}

private class FakeAlarmSettingsRepository(
    enabled: Boolean,
) : AlarmSettingsRepository {
    private val alarmEnabledFlow = MutableStateFlow(enabled)

    override val alarmEnabled: Flow<Boolean> = alarmEnabledFlow.asStateFlow()

    override suspend fun isAlarmEnabled(): Boolean = alarmEnabledFlow.value

    override suspend fun setAlarmEnabled(enabled: Boolean) {
        alarmEnabledFlow.value = enabled
    }
}

private class FakeBatteryLowReceiverRegistrar : BatteryLowReceiverRegistrar {
    val enabledValues = mutableListOf<Boolean>()

    override fun setBatteryLowReceiverEnabled(enabled: Boolean) {
        enabledValues += enabled
    }
}

private class FakeAlarmController : AlarmController {
    override val state: AlarmState = AlarmState.Idle
    val stopReasons = mutableListOf<AlarmStopReason>()

    override suspend fun startAlarm(reason: AlarmStartReason): AlarmStartResult {
        return AlarmStartResult.Started(AlarmState.Active(reason))
    }

    override fun stopAlarm(reason: AlarmStopReason): AlarmStopResult {
        stopReasons += reason
        return AlarmStopResult.AlreadyIdle
    }
}
