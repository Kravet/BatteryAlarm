package com.example.batteryalarm.ui

import com.example.batteryalarm.alarm.BatteryLowAlarmHandler
import com.example.batteryalarm.domain.AlarmController
import com.example.batteryalarm.domain.AlarmStartReason
import com.example.batteryalarm.domain.AlarmStartResult
import com.example.batteryalarm.domain.AlarmSettingsRepository
import com.example.batteryalarm.domain.AlarmState
import com.example.batteryalarm.domain.AlarmStopReason
import com.example.batteryalarm.domain.AlarmStopResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun TestScope.createViewModel(
        alarmSettingsRepository: FakeAlarmSettingsRepository = FakeAlarmSettingsRepository(enabled = false),
        alarmController: FakeAlarmController = FakeAlarmController(),
    ): MainViewModel {
        val handler = BatteryLowAlarmHandler(
            alarmController = alarmController,
            scope = this,
        )
        return MainViewModel(
            alarmSettingsRepository = alarmSettingsRepository,
            batteryLowAlarmHandler = handler,
        )
    }

    @Test
    fun `initial state uses stored alarm enabled flag`() = runTest {
        val viewModel = createViewModel(
            alarmSettingsRepository = FakeAlarmSettingsRepository(enabled = true),
        )

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isAlarmEnabled)
    }

    @Test
    fun `alarm enabled change stores enabled flag and updates state`() = runTest {
        val repository = FakeAlarmSettingsRepository(enabled = false)
        val viewModel = createViewModel(alarmSettingsRepository = repository)

        advanceUntilIdle()
        viewModel.onAlarmEnabledChange(true)
        advanceUntilIdle()

        assertTrue(repository.isAlarmEnabled())
        assertTrue(viewModel.uiState.value.isAlarmEnabled)
    }

    @Test
    fun `alarm disabled change stores disabled flag and updates state`() = runTest {
        val repository = FakeAlarmSettingsRepository(enabled = true)
        val viewModel = createViewModel(alarmSettingsRepository = repository)

        advanceUntilIdle()
        viewModel.onAlarmEnabledChange(false)
        advanceUntilIdle()

        assertFalse(repository.isAlarmEnabled())
        assertFalse(viewModel.uiState.value.isAlarmEnabled)
    }

    @Test
    fun `alarm enabled change updates state from saved value emitted by repository`() = runTest {
        val repository = FakeAlarmSettingsRepository(
            enabled = false,
            valueSavedAfterSet = false,
        )
        val viewModel = createViewModel(alarmSettingsRepository = repository)

        advanceUntilIdle()
        viewModel.onAlarmEnabledChange(true)
        advanceUntilIdle()

        assertFalse(repository.isAlarmEnabled())
        assertFalse(viewModel.uiState.value.isAlarmEnabled)
    }

    @Test
    fun `alarm enabled change restores persisted state and emits failure side effect when repository throws`() =
        runTest {
            val repository = FakeAlarmSettingsRepository(
                enabled = false,
                exceptionOnSet = IllegalStateException("Write failed"),
            )
            val viewModel = createViewModel(alarmSettingsRepository = repository)
            val sideEffects = mutableListOf<MainSideEffect>()
            val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.sideEffects
                    .take(1)
                    .toList(sideEffects)
            }

            advanceUntilIdle()
            viewModel.onAlarmEnabledChange(true)
            advanceUntilIdle()

            collectJob.join()
            assertFalse(repository.isAlarmEnabled())
            assertFalse(viewModel.uiState.value.isAlarmEnabled)
            assertEquals(
                listOf(MainSideEffect.ShowAlarmSettingsChangeFailed),
                sideEffects,
            )
        }

    @Test
    fun `alarm enabled change does not handle save cancellation as failure`() = runTest {
        val repository = FakeAlarmSettingsRepository(
            enabled = false,
            exceptionOnSet = CancellationException("Save cancelled"),
        )
        val viewModel = createViewModel(alarmSettingsRepository = repository)
        val sideEffects = mutableListOf<MainSideEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.sideEffects.toCollection(sideEffects)
        }

        advanceUntilIdle()
        viewModel.onAlarmEnabledChange(true)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isAlarmEnabled)
        assertEquals(emptyList<MainSideEffect>(), sideEffects)
    }

    @Test
    fun `initial state is disabled when stored flag is disabled`() = runTest {
        val viewModel = createViewModel(
            alarmSettingsRepository = FakeAlarmSettingsRepository(enabled = false),
        )

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isAlarmEnabled)
    }

    @Test
    fun `test alarm uses shared trigger after delay without changing main screen state`() = runTest {
        val alarmController = FakeAlarmController()
        val viewModel = createViewModel(
            alarmSettingsRepository = FakeAlarmSettingsRepository(enabled = false),
            alarmController = alarmController,
        )

        advanceUntilIdle()
        viewModel.onTestAlarmClick()

        assertTrue(viewModel.uiState.value.isTestAlarmPending)
        assertEquals(emptyList<AlarmStartReason>(), alarmController.startedReasons)

        advanceTimeBy(BatteryLowAlarmHandler.TEST_ALARM_DELAY_MS)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isTestAlarmPending)
        assertEquals(listOf(AlarmStartReason.TestAlarmFlow), alarmController.startedReasons)
    }
}

private class FakeAlarmSettingsRepository(
    enabled: Boolean,
    private val valueSavedAfterSet: Boolean? = null,
    private val exceptionOnSet: Exception? = null,
) : AlarmSettingsRepository {
    private val alarmEnabledFlow = MutableStateFlow(enabled)

    override val alarmEnabled: Flow<Boolean> = alarmEnabledFlow.asStateFlow()

    override suspend fun isAlarmEnabled(): Boolean {
        return alarmEnabledFlow.value
    }

    override suspend fun setAlarmEnabled(enabled: Boolean) {
        exceptionOnSet?.let { throw it }
        alarmEnabledFlow.value = valueSavedAfterSet ?: enabled
    }
}

private class FakeAlarmController(
    initialState: AlarmState = AlarmState.Idle,
    private val startException: Exception? = null,
) : AlarmController {
    override var state: AlarmState = initialState
        private set

    val startedReasons = mutableListOf<AlarmStartReason>()
    val stoppedReasons = mutableListOf<AlarmStopReason>()

    override suspend fun startAlarm(reason: AlarmStartReason): AlarmStartResult {
        startException?.let { throw it }
        startedReasons += reason

        val activeState = state as? AlarmState.Active
        if (activeState != null) {
            return AlarmStartResult.AlreadyActive(activeState)
        }

        val newState = AlarmState.Active(reason)
        state = newState
        return AlarmStartResult.Started(newState)
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

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
