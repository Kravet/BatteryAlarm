package com.example.batteryalarm.ui.feature

import com.example.batteryalarm.alarm.BatteryLowAlarmHandler
import com.example.batteryalarm.analytics.AppAnalytics
import com.example.batteryalarm.domain.AlarmController
import com.example.batteryalarm.domain.AlarmSettingsRepository
import com.example.batteryalarm.domain.AlarmState
import com.example.batteryalarm.ui.MainSideEffect
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Presentation state holder for alarm controls and the test-alarm countdown.
 * Work started from the screen uses its ViewModel scope; the existing alarm handler
 * remains application-scoped for the actual alarm operation.
 */
class AlarmFeature @Inject constructor(
    private val settingsRepository: AlarmSettingsRepository,
    private val batteryLowAlarmHandler: BatteryLowAlarmHandler,
    private val alarmController: AlarmController,
    private val analytics: AppAnalytics,
) {
    private val _state = MutableStateFlow(AlarmFeatureState())
    val state: StateFlow<AlarmFeatureState> = _state.asStateFlow()

    private val _sideEffects = MutableSharedFlow<MainSideEffect>(extraBufferCapacity = 1)
    val sideEffects: SharedFlow<MainSideEffect> = _sideEffects.asSharedFlow()

    fun observe(scope: CoroutineScope) {
        scope.launch {
            settingsRepository.alarmEnabled
                .catch { if (it is CancellationException) throw it }
                .collect { enabled ->
                    _state.update { it.copy(isEnabled = enabled) }
                }
        }
    }

    fun isAlarmActive(): Boolean = alarmController.state is AlarmState.Active

    fun toggle(scope: CoroutineScope) {
        scope.launch {
            try {
                settingsRepository.toggleAlarmEnabled()
                analytics.logAlarmToggled(enabled = settingsRepository.isAlarmEnabled())
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _sideEffects.emit(MainSideEffect.ShowAlarmSettingsChangeFailed)
            }
        }
    }

    fun onNotificationPermissionDenied() {
        analytics.logNotificationPermissionDenied()
    }

    fun startTestAlarm(scope: CoroutineScope) {
        if (_state.value.isTestAlarmPending) return

        analytics.logTestAlarmStarted()
        _state.update {
            it.copy(
                isTestAlarmPending = true,
                testAlarmSecondsRemaining = BatteryLowAlarmHandler.TEST_ALARM_DELAY_SECONDS,
            )
        }
        batteryLowAlarmHandler.handleTestAlarm(
            onTick = { secondsRemaining ->
                _state.update {
                    it.copy(
                        isTestAlarmPending = true,
                        testAlarmSecondsRemaining = secondsRemaining,
                    )
                }
            },
            onFinished = {
                _state.update {
                    it.copy(isTestAlarmPending = false, testAlarmSecondsRemaining = null)
                }
            },
            onFailed = {
                analytics.logTestAlarmFailed()
                _state.update {
                    it.copy(isTestAlarmPending = false, testAlarmSecondsRemaining = null)
                }
                scope.launch {
                    _sideEffects.emit(MainSideEffect.ShowTestAlarmStartFailed)
                }
            },
        )
    }
}

data class AlarmFeatureState(
    val isEnabled: Boolean = false,
    val isTestAlarmPending: Boolean = false,
    val testAlarmSecondsRemaining: Int? = null,
)
