package com.example.batteryalarm.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.batteryalarm.ui.feature.AlarmFeature
import com.example.batteryalarm.ui.feature.PrivacyFeature
import com.example.batteryalarm.ui.feature.UpdateFeature
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * Screen-level UDF owner: composes feature states and routes UI actions.
 * Each feature handles its own presentation orchestration.
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val alarm: AlarmFeature,
    private val updates: UpdateFeature,
    private val privacy: PrivacyFeature,
) : ViewModel() {
    val uiState: StateFlow<MainUiState> = combine(
        alarm.state,
        updates.state,
        privacy.state,
    ) { alarmState, updateState, privacyState ->
        MainUiState.from(
            alarmEnabled = alarmState.isEnabled,
            isTestAlarmPending = alarmState.isTestAlarmPending,
            testAlarmSecondsRemaining = alarmState.testAlarmSecondsRemaining,
            pendingUpdate = updateState.pendingUpdate,
            analyticsEnabled = privacyState.analyticsEnabled,
            crashlyticsEnabled = privacyState.crashlyticsEnabled,
            decisionRecorded = privacyState.decisionRecorded,
            privacySheetMode = privacyState.privacySheetMode,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = MainUiState.from(alarmEnabled = false, isTestAlarmPending = false),
    )

    val sideEffects: SharedFlow<MainSideEffect> = alarm.sideEffects

    init {
        alarm.observe(viewModelScope)
        privacy.observe(viewModelScope)
        updates.checkOnStart(viewModelScope)
    }

    fun isAlarmActive(): Boolean = alarm.isAlarmActive()

    fun onAlarmToggleClick() = alarm.toggle(viewModelScope)
    fun onNotificationPermissionDenied() = alarm.onNotificationPermissionDenied()
    fun onTestAlarmClick() = alarm.startTestAlarm(viewModelScope)

    fun onUpdateClick() = updates.openPending(viewModelScope)

    fun onPrivacyDiagnosticsClick() = privacy.openSettings()
    fun onPrivacySheetDismiss() = privacy.dismissSheet()
    fun onAllowBothPrivacy() = privacy.allowBoth(viewModelScope)
    fun onDeclineBothPrivacy() = privacy.declineBoth(viewModelScope)
    fun onSaveCustomPrivacyChoices(analyticsEnabled: Boolean, crashlyticsEnabled: Boolean) =
        privacy.saveCustom(viewModelScope, analyticsEnabled, crashlyticsEnabled)
    fun onAnalyticsConsentChange(enabled: Boolean) = privacy.changeAnalytics(viewModelScope, enabled)
    fun onCrashlyticsConsentChange(enabled: Boolean) = privacy.changeCrashlytics(viewModelScope, enabled)
}

sealed interface MainSideEffect {
    data object ShowAlarmSettingsChangeFailed : MainSideEffect
    data object ShowTestAlarmStartFailed : MainSideEffect
}
