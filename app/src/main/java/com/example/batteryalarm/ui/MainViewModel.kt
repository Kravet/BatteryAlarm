package com.example.batteryalarm.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.batteryalarm.R
import com.example.batteryalarm.alarm.BatteryLowAlarmHandler
import com.example.batteryalarm.analytics.AppAnalytics
import com.example.batteryalarm.domain.AlarmController
import com.example.batteryalarm.domain.AlarmSettingsRepository
import com.example.batteryalarm.domain.AlarmState
import com.example.batteryalarm.domain.privacy.PrivacyConsentRepository
import com.example.batteryalarm.domain.update.CheckForAppUpdate
import com.example.batteryalarm.domain.update.UpdateCheckAttemptStore
import com.example.batteryalarm.domain.update.UpdateCheckResult
import com.example.batteryalarm.privacy.FirebaseConsentApplier
import com.example.batteryalarm.ui.privacy.PrivacySheetMode
import com.example.batteryalarm.update.UpdateCheckLog
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
    private val analytics: AppAnalytics,
    private val privacyConsentRepository: PrivacyConsentRepository,
    private val firebaseConsentApplier: FirebaseConsentApplier,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainUiState.from(alarmEnabled = false, isTestAlarmPending = false))
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _sideEffects = MutableSharedFlow<MainSideEffect>(
        extraBufferCapacity = 1,
    )
    val sideEffects: SharedFlow<MainSideEffect> = _sideEffects.asSharedFlow()

    private var onboardingDismissedThisSession = false

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
                        currentState.copy(
                            isEnabled = alarmEnabled,
                            toggleContentDescriptionRes = if (alarmEnabled) {
                                R.string.disable_battery_alarm
                            } else {
                                R.string.enable_battery_alarm
                            },
                        )
                    }
                }
        }
        viewModelScope.launch {
            privacyConsentRepository.consent
                .catch { exception ->
                    if (exception is CancellationException) {
                        throw exception
                    }
                }
                .collect { consent ->
                    firebaseConsentApplier.apply(
                        analyticsEnabled = consent.effectiveAnalyticsEnabled,
                        crashlyticsEnabled = consent.effectiveCrashlyticsEnabled,
                    )
                    _uiState.update { currentState ->
                        val hasCurrentDecision = !consent.needsDecision
                        val shouldShowOnboarding = consent.needsDecision &&
                            !onboardingDismissedThisSession &&
                            currentState.privacySheetMode != PrivacySheetMode.SETTINGS
                        currentState.copy(
                            analyticsEnabled = consent.analyticsEnabled,
                            crashlyticsEnabled = consent.crashlyticsEnabled,
                            decisionRecorded = hasCurrentDecision,
                            privacySheetMode = when {
                                hasCurrentDecision &&
                                    currentState.privacySheetMode == PrivacySheetMode.ONBOARDING -> null
                                shouldShowOnboarding -> PrivacySheetMode.ONBOARDING
                                else -> currentState.privacySheetMode
                            },
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
                analytics.logAlarmToggled(enabled = alarmSettingsRepository.isAlarmEnabled())
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

    fun onTestAlarmClick() {
        if (_uiState.value.isTestAlarmPending) {
            return
        }

        analytics.logTestAlarmStarted()
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
                analytics.logTestAlarmFailed()
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
        analytics.logUpdateClicked(version = pending.versionName)
        updateOpener.openReleasePage(pending.releasePageUrl)
        _uiState.update { it.copy(pendingUpdate = null) }
        viewModelScope.launch {
            updateCheckAttemptStore.clearPendingUpdate()
        }
    }

    fun onPrivacyDiagnosticsClick() {
        _uiState.update { it.copy(privacySheetMode = PrivacySheetMode.SETTINGS) }
    }

    fun onPrivacySheetDismiss() {
        val mode = _uiState.value.privacySheetMode
        if (mode == PrivacySheetMode.ONBOARDING && !_uiState.value.decisionRecorded) {
            onboardingDismissedThisSession = true
        }
        _uiState.update { it.copy(privacySheetMode = null) }
    }

    fun onAllowBothPrivacy() {
        persistConsent(analyticsEnabled = true, crashlyticsEnabled = true)
    }

    fun onDeclineBothPrivacy() {
        persistConsent(analyticsEnabled = false, crashlyticsEnabled = false)
    }

    fun onSaveCustomPrivacyChoices(analyticsEnabled: Boolean, crashlyticsEnabled: Boolean) {
        persistConsent(
            analyticsEnabled = analyticsEnabled,
            crashlyticsEnabled = crashlyticsEnabled,
        )
    }

    fun onAnalyticsConsentChange(enabled: Boolean) {
        persistConsent(
            analyticsEnabled = enabled,
            crashlyticsEnabled = _uiState.value.crashlyticsEnabled,
        )
    }

    fun onCrashlyticsConsentChange(enabled: Boolean) {
        persistConsent(
            analyticsEnabled = _uiState.value.analyticsEnabled,
            crashlyticsEnabled = enabled,
        )
    }

    private fun persistConsent(analyticsEnabled: Boolean, crashlyticsEnabled: Boolean) {
        viewModelScope.launch {
            privacyConsentRepository.setConsent(
                analyticsEnabled = analyticsEnabled,
                crashlyticsEnabled = crashlyticsEnabled,
            )
            firebaseConsentApplier.apply(
                analyticsEnabled = analyticsEnabled,
                crashlyticsEnabled = crashlyticsEnabled,
            )
            _uiState.update {
                it.copy(
                    analyticsEnabled = analyticsEnabled,
                    crashlyticsEnabled = crashlyticsEnabled,
                    decisionRecorded = true,
                    privacySheetMode = if (it.privacySheetMode == PrivacySheetMode.ONBOARDING) {
                        null
                    } else {
                        it.privacySheetMode
                    },
                )
            }
        }
    }

    private suspend fun runUpdateCheck() {
        UpdateCheckLog.d("runUpdateCheck start")
        try {
            when (val result = checkForAppUpdate()) {
                is UpdateCheckResult.Available -> {
                    UpdateCheckLog.d(
                        "runUpdateCheck Available version=${result.update.versionName} " +
                            "url=${result.update.releasePageUrl} alarmActive=${isAlarmActive()}",
                    )
                    if (!isAlarmActive()) {
                        val update = AppUpdateUiModel.from(result.update)
                        analytics.logUpdateAvailableShown(version = update.versionName)
                        _uiState.update { current ->
                            current.copy(pendingUpdate = update)
                        }
                    }
                }
                UpdateCheckResult.Disabled -> UpdateCheckLog.d("runUpdateCheck Disabled")
                UpdateCheckResult.SkippedWithinInterval ->
                    UpdateCheckLog.d("runUpdateCheck SkippedWithinInterval")
                UpdateCheckResult.UpToDate -> UpdateCheckLog.d("runUpdateCheck UpToDate")
                UpdateCheckResult.NoUsableRelease ->
                    UpdateCheckLog.d("runUpdateCheck NoUsableRelease")
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            UpdateCheckLog.w("runUpdateCheck failed: ${exception.message}")
        }
    }
}

sealed interface MainSideEffect {
    data object ShowAlarmSettingsChangeFailed : MainSideEffect
    data object ShowTestAlarmStartFailed : MainSideEffect
}
