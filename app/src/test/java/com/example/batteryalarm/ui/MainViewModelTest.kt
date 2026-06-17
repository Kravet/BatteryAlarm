package com.example.batteryalarm.ui

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

    @Test
    fun `initial state uses stored alarm enabled flag`() = runTest {
        val viewModel = MainViewModel(
            alarmSettingsRepository = FakeAlarmSettingsRepository(enabled = true),
            alarmController = FakeAlarmController(),
        )

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isAlarmEnabled)
    }

    @Test
    fun `alarm enabled change stores enabled flag and updates state`() = runTest {
        val repository = FakeAlarmSettingsRepository(enabled = false)
        val viewModel = MainViewModel(repository, FakeAlarmController())

        advanceUntilIdle()
        viewModel.onAlarmEnabledChange(true)
        advanceUntilIdle()

        assertTrue(repository.isAlarmEnabled())
        assertTrue(viewModel.uiState.value.isAlarmEnabled)
    }

    @Test
    fun `alarm disabled change stores disabled flag and updates state`() = runTest {
        val repository = FakeAlarmSettingsRepository(enabled = true)
        val viewModel = MainViewModel(repository, FakeAlarmController())

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
        val viewModel = MainViewModel(repository, FakeAlarmController())

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
            val viewModel = MainViewModel(repository, FakeAlarmController())
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
        val viewModel = MainViewModel(repository, FakeAlarmController())
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
        val viewModel = MainViewModel(
            alarmSettingsRepository = FakeAlarmSettingsRepository(enabled = false),
            alarmController = FakeAlarmController(),
        )

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isAlarmEnabled)
    }

    @Test
    fun `test alarm starts alarm flow and requests alarm screen launch`() = runTest {
        val alarmController = FakeAlarmController()
        val viewModel = MainViewModel(
            alarmSettingsRepository = FakeAlarmSettingsRepository(enabled = false),
            alarmController = alarmController,
        )
        val sideEffects = mutableListOf<MainSideEffect>()
        val collectJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.sideEffects
                .take(1)
                .toList(sideEffects)
        }

        advanceUntilIdle()
        viewModel.onTestAlarmClick()
        advanceUntilIdle()

        collectJob.join()
        assertEquals(listOf(AlarmStartReason.TestAlarmFlow), alarmController.startedReasons)
        assertEquals(listOf(MainSideEffect.LaunchAlarmScreen), sideEffects)
        assertFalse(viewModel.uiState.value.isAlarmScreenVisible)
    }

    @Test
    fun `alarm intent shows alarm screen when alarm is active`() = runTest {
        val viewModel = MainViewModel(
            alarmSettingsRepository = FakeAlarmSettingsRepository(enabled = true),
            alarmController = FakeAlarmController(
                initialState = AlarmState.Active(AlarmStartReason.SystemLowBattery),
            ),
        )

        advanceUntilIdle()
        viewModel.onAlarmIntentReceived(isAlarmIntent = true)

        assertTrue(viewModel.uiState.value.isAlarmScreenVisible)
    }

    @Test
    fun `stop alarm stops outputs and closes alarm screen`() = runTest {
        val alarmController = FakeAlarmController(
            initialState = AlarmState.Active(AlarmStartReason.TestAlarmFlow),
        )
        val viewModel = MainViewModel(
            alarmSettingsRepository = FakeAlarmSettingsRepository(enabled = true),
            alarmController = alarmController,
        )

        advanceUntilIdle()
        viewModel.onTestAlarmClick()
        advanceUntilIdle()
        viewModel.onAlarmIntentReceived(isAlarmIntent = true)
        viewModel.onStopAlarmClick()

        assertEquals(listOf(AlarmStopReason.UserDismissed), alarmController.stoppedReasons)
        assertFalse(viewModel.uiState.value.isAlarmScreenVisible)
    }

    @Test
    fun `alarm setting updates preserve visible alarm screen`() = runTest {
        val repository = FakeAlarmSettingsRepository(enabled = false)
        val viewModel = MainViewModel(
            alarmSettingsRepository = repository,
            alarmController = FakeAlarmController(),
        )

        advanceUntilIdle()
        viewModel.onTestAlarmClick()
        advanceUntilIdle()
        viewModel.onAlarmIntentReceived(isAlarmIntent = true)
        repository.setAlarmEnabled(true)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isAlarmEnabled)
        assertTrue(viewModel.uiState.value.isAlarmScreenVisible)
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
    private val testDispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
