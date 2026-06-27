package com.example.batteryalarm.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultAlarmSettingsCoordinatorTest {
    @Test
    fun `initial enabled setting starts battery low monitoring`() = runTest {
        val repository = CoordinatorFakeAlarmSettingsRepository(enabled = true)
        val monitoring = FakeBatteryLowMonitoring()
        val alarmController = FakeAlarmController()
        val coordinator = DefaultAlarmSettingsCoordinator(
            alarmSettingsRepository = repository,
            batteryLowMonitoring = monitoring,
            alarmController = alarmController,
            scope = CoroutineScope(backgroundScope.coroutineContext + UnconfinedTestDispatcher(testScheduler)),
        )

        coordinator.start()
        advanceUntilIdle()

        assertEquals(listOf(true), monitoring.enabledValues)
        assertEquals(emptyList<AlarmStopReason>(), alarmController.stopReasons)
    }

    @Test
    fun `initial disabled setting stops monitoring and alarm`() = runTest {
        val repository = CoordinatorFakeAlarmSettingsRepository(enabled = false)
        val monitoring = FakeBatteryLowMonitoring()
        val alarmController = FakeAlarmController()
        val coordinator = DefaultAlarmSettingsCoordinator(
            alarmSettingsRepository = repository,
            batteryLowMonitoring = monitoring,
            alarmController = alarmController,
            scope = CoroutineScope(backgroundScope.coroutineContext + UnconfinedTestDispatcher(testScheduler)),
        )

        coordinator.start()
        advanceUntilIdle()

        assertEquals(listOf(false), monitoring.enabledValues)
        assertEquals(listOf(AlarmStopReason.SettingsDisabled), alarmController.stopReasons)
    }

    @Test
    fun `setting change from enabled to disabled stops monitoring and alarm`() = runTest {
        val repository = CoordinatorFakeAlarmSettingsRepository(enabled = true)
        val monitoring = FakeBatteryLowMonitoring()
        val alarmController = FakeAlarmController()
        val coordinator = DefaultAlarmSettingsCoordinator(
            alarmSettingsRepository = repository,
            batteryLowMonitoring = monitoring,
            alarmController = alarmController,
            scope = CoroutineScope(backgroundScope.coroutineContext + UnconfinedTestDispatcher(testScheduler)),
        )

        coordinator.start()
        advanceUntilIdle()
        repository.setAlarmEnabled(false)
        advanceUntilIdle()

        assertEquals(listOf(true, false), monitoring.enabledValues)
        assertEquals(listOf(AlarmStopReason.SettingsDisabled), alarmController.stopReasons)
    }

    @Test
    fun `start is idempotent and does not create duplicate collectors`() = runTest {
        val repository = CoordinatorFakeAlarmSettingsRepository(enabled = true)
        val monitoring = FakeBatteryLowMonitoring()
        val alarmController = FakeAlarmController()
        val coordinator = DefaultAlarmSettingsCoordinator(
            alarmSettingsRepository = repository,
            batteryLowMonitoring = monitoring,
            alarmController = alarmController,
            scope = CoroutineScope(backgroundScope.coroutineContext + UnconfinedTestDispatcher(testScheduler)),
        )

        coordinator.start()
        coordinator.start()
        advanceUntilIdle()
        repository.setAlarmEnabled(false)
        advanceUntilIdle()

        assertEquals(listOf(true, false), monitoring.enabledValues)
        assertEquals(listOf(AlarmStopReason.SettingsDisabled), alarmController.stopReasons)
    }
}

private class CoordinatorFakeAlarmSettingsRepository(
    enabled: Boolean,
) : AlarmSettingsRepository {
    private val alarmEnabledFlow = MutableStateFlow(enabled)

    override val alarmEnabled: Flow<Boolean> = alarmEnabledFlow.asStateFlow()

    override suspend fun isAlarmEnabled(): Boolean = alarmEnabledFlow.value

    override suspend fun setAlarmEnabled(enabled: Boolean) {
        alarmEnabledFlow.value = enabled
    }

    override suspend fun toggleAlarmEnabled() {
        setAlarmEnabled(!isAlarmEnabled())
    }
}

private class FakeBatteryLowMonitoring : BatteryLowMonitoring {
    val enabledValues = mutableListOf<Boolean>()

    override fun setEnabled(enabled: Boolean) {
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
