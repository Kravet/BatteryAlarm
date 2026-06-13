package com.example.batteryalarm.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.batteryalarm.domain.AlarmSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor(
    private val alarmSettingsRepository: AlarmSettingsRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _sideEffects = MutableSharedFlow<MainSideEffect>(
        extraBufferCapacity = 1,
    )
    val sideEffects: SharedFlow<MainSideEffect> = _sideEffects.asSharedFlow()

    init {
        viewModelScope.launch {
            val alarmEnabled = try {
                alarmSettingsRepository.isAlarmEnabled()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.value.isAlarmEnabled
            }

            _uiState.value = MainUiState(isAlarmEnabled = alarmEnabled)
        }
    }

    fun onAlarmEnabledChange(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val savedAlarmEnabled = alarmSettingsRepository.setAlarmEnabled(enabled)
                _uiState.value = MainUiState(isAlarmEnabled = savedAlarmEnabled)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                val currentAlarmEnabled = try {
                    alarmSettingsRepository.isAlarmEnabled()
                } catch (cancellationException: CancellationException) {
                    throw cancellationException
                } catch (readException: Exception) {
                    _uiState.value.isAlarmEnabled
                }

                _uiState.value = MainUiState(isAlarmEnabled = currentAlarmEnabled)
                _sideEffects.emit(MainSideEffect.ShowAlarmSettingsChangeFailed)
            }
        }
    }
}

data class MainUiState(
    val isAlarmEnabled: Boolean = false,
)

sealed interface MainSideEffect {
    data object ShowAlarmSettingsChangeFailed : MainSideEffect
}
