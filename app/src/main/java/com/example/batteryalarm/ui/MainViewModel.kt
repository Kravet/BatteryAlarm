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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val alarmSettingsRepository: AlarmSettingsRepository,
    private val batteryLowAlarmHandler: BatteryLowAlarmHandler,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
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
                    _uiState.value = _uiState.value.copy(isAlarmEnabled = alarmEnabled)
                }
        }
    }

    fun onAlarmEnabledChange(enabled: Boolean) {
        viewModelScope.launch {
            try {
                alarmSettingsRepository.setAlarmEnabled(enabled)
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

        _uiState.value = _uiState.value.copy(isTestAlarmPending = true)
        batteryLowAlarmHandler.handleTestAlarm(
            onFinished = {
                _uiState.value = _uiState.value.copy(isTestAlarmPending = false)
            },
            onFailed = {
                _uiState.value = _uiState.value.copy(isTestAlarmPending = false)
                viewModelScope.launch {
                    _sideEffects.emit(MainSideEffect.ShowTestAlarmStartFailed)
                }
            },
        )
    }
}

data class MainUiState(
    val isAlarmEnabled: Boolean = false,
    val isTestAlarmPending: Boolean = false,
)

sealed interface MainSideEffect {
    data object ShowAlarmSettingsChangeFailed : MainSideEffect
    data object ShowTestAlarmStartFailed : MainSideEffect
}
