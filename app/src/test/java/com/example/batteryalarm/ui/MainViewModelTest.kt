package com.example.batteryalarm.ui

import com.example.batteryalarm.domain.AlarmSettingsRepository
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
        val viewModel = MainViewModel(FakeAlarmSettingsRepository(enabled = true))

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isAlarmEnabled)
    }

    @Test
    fun `alarm enabled change stores enabled flag and updates state`() = runTest {
        val repository = FakeAlarmSettingsRepository(enabled = false)
        val viewModel = MainViewModel(repository)

        advanceUntilIdle()
        viewModel.onAlarmEnabledChange(true)
        advanceUntilIdle()

        assertTrue(repository.isAlarmEnabled())
        assertTrue(viewModel.uiState.value.isAlarmEnabled)
    }

    @Test
    fun `alarm disabled change stores disabled flag and updates state`() = runTest {
        val repository = FakeAlarmSettingsRepository(enabled = true)
        val viewModel = MainViewModel(repository)

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
        val viewModel = MainViewModel(repository)

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
            val viewModel = MainViewModel(repository)
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
        val viewModel = MainViewModel(repository)
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
        val viewModel = MainViewModel(FakeAlarmSettingsRepository(enabled = false))

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isAlarmEnabled)
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
