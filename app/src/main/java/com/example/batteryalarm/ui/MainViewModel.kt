package com.example.batteryalarm.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.batteryalarm.domain.AlarmController
import com.example.batteryalarm.domain.AlarmStartReason
import com.example.batteryalarm.domain.AlarmStartResult
import com.example.batteryalarm.domain.AlarmSettingsRepository
import com.example.batteryalarm.domain.AlarmState
import com.example.batteryalarm.domain.AlarmStopReason
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
    private val alarmController: AlarmController,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    //TODO single time event or channel the that google propose
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

    fun onAlarmIntentReceived(isAlarmIntent: Boolean) {
        if (!isAlarmIntent) {
            return
        }
        syncAlarmScreenVisibility()
    }

    fun syncAlarmScreenVisibility() {
        if (alarmController.state is AlarmState.Active) {
            _uiState.value = _uiState.value.copy(isAlarmScreenVisible = true)
        }
    }

    fun onTestAlarmClick() {
        viewModelScope.launch {
            try {
                when (alarmController.startAlarm(AlarmStartReason.TestAlarmFlow)) {
                    is AlarmStartResult.Started,
                    is AlarmStartResult.AlreadyActive,
                    -> _sideEffects.emit(MainSideEffect.LaunchAlarmScreen)

                    AlarmStartResult.Disabled -> Unit
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _sideEffects.emit(MainSideEffect.ShowTestAlarmStartFailed)
            }
        }
    }

    fun onStopAlarmClick() {
        alarmController.stopAlarm(AlarmStopReason.UserDismissed)
        _uiState.value = _uiState.value.copy(isAlarmScreenVisible = false)
    }
}

data class MainUiState(
    val isAlarmEnabled: Boolean = false,
    val isAlarmScreenVisible: Boolean = false,
)

sealed interface MainSideEffect {
    data object ShowAlarmSettingsChangeFailed : MainSideEffect
    data object ShowTestAlarmStartFailed : MainSideEffect
    data object LaunchAlarmScreen : MainSideEffect
}
