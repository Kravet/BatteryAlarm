package com.example.batteryalarm.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.batteryalarm.alarm.BatteryLowAlarmHandler
import com.example.batteryalarm.domain.AlarmController
import com.example.batteryalarm.domain.AlarmSettingsRepository
import com.example.batteryalarm.domain.AlarmState
import com.example.batteryalarm.domain.update.CheckForAppUpdate
import com.example.batteryalarm.domain.update.UpdateCheckAttemptStore
import com.example.batteryalarm.domain.update.UpdateCheckResult
import com.example.batteryalarm.update.UpdateOpener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val alarmSettingsRepository: AlarmSettingsRepository,
    private val batteryLowAlarmHandler: BatteryLowAlarmHandler,
    private val alarmController: AlarmController,
    private val checkForAppUpdate: CheckForAppUpdate,
    private val updateCheckAttemptStore: UpdateCheckAttemptStore,
    private val updateOpener: UpdateOpener,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState.from(alarmEnabled = false, isTestAlarmPending = false))
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _sideEffects = MutableSharedFlow<MainSideEffect>(
        extraBufferCapacity = 1,
    )
    val sideEffects: SharedFlow<MainSideEffect> = _sideEffects.asSharedFlow()

    init {
        viewModelScope.launch {
            alarmSettingsRepository.alarmEnabled
                .catch { exception ->
                    if (exception is CancellationException) {
                        throw exception
                    }
                }
                .collect { alarmEnabled ->
                    _uiState.update { currentState ->
                        MainUiState.from(
                            alarmEnabled = alarmEnabled,
                            isTestAlarmPending = currentState.isTestAlarmPending,
                            testAlarmSecondsRemaining = currentState.testAlarmSecondsRemaining,
                            pendingUpdate = currentState.pendingUpdate,
                        )
                    }
                }
        }
        viewModelScope.launch {
            runUpdateCheck()
        }
    }

    fun isAlarmActive(): Boolean = alarmController.state is AlarmState.Active

    fun onAlarmToggleClick() {
        viewModelScope.launch {
            try {
                alarmSettingsRepository.toggleAlarmEnabled()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _sideEffects.emit(MainSideEffect.ShowAlarmSettingsChangeFailed)
            }
        }
    }

    fun onTestAlarmClick() {
        if (_uiState.value.isTestAlarmPending) {
            return
        }

        _uiState.update {
            it.copy(
                isTestAlarmPending = true,
                testAlarmSecondsRemaining = BatteryLowAlarmHandler.TEST_ALARM_DELAY_SECONDS,
            )
        }
        batteryLowAlarmHandler.handleTestAlarm(
            onTick = { secondsRemaining ->
                _uiState.update {
                    it.copy(
                        isTestAlarmPending = true,
                        testAlarmSecondsRemaining = secondsRemaining,
                    )
                }
            },
            onFinished = {
                _uiState.update {
                    it.copy(
                        isTestAlarmPending = false,
                        testAlarmSecondsRemaining = null,
                    )
                }
            },
            onFailed = {
                _uiState.update {
                    it.copy(
                        isTestAlarmPending = false,
                        testAlarmSecondsRemaining = null,
                    )
                }
                viewModelScope.launch {
                    _sideEffects.emit(MainSideEffect.ShowTestAlarmStartFailed)
                }
            },
        )
    }

    fun onUpdateClick() {
        val pending = _uiState.value.pendingUpdate ?: return
        updateOpener.openReleasePage(pending.releasePageUrl)
        _uiState.update { it.copy(pendingUpdate = null) }
        viewModelScope.launch {
            updateCheckAttemptStore.clearPendingUpdate()
        }
    }

    private suspend fun runUpdateCheck() {
        try {
            when (val result = checkForAppUpdate()) {
                is UpdateCheckResult.Available -> {
                    if (!isAlarmActive()) {
                        _uiState.update { current ->
                            current.copy(pendingUpdate = AppUpdateUiModel.from(result.update))
                        }
                    }
                }
                UpdateCheckResult.Disabled,
                UpdateCheckResult.SkippedWithinInterval,
                UpdateCheckResult.UpToDate,
                UpdateCheckResult.NoUsableRelease,
                -> Unit
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            // Auto update checks fail silently.
        }
    }
}

sealed interface MainSideEffect {
    data object ShowAlarmSettingsChangeFailed : MainSideEffect
    data object ShowTestAlarmStartFailed : MainSideEffect
}
