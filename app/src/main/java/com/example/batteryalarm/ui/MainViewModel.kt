package com.example.batteryalarm.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.batteryalarm.alarm.BatteryLowAlarmHandler
import com.example.batteryalarm.domain.AlarmSettingsRepository
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
                        )
                    }
                }
        }
    }

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
}

sealed interface MainSideEffect {
    data object ShowAlarmSettingsChangeFailed : MainSideEffect
    data object ShowTestAlarmStartFailed : MainSideEffect
}
