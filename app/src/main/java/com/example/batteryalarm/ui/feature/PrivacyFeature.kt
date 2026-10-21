package com.example.batteryalarm.ui.feature

import com.example.batteryalarm.domain.privacy.PrivacyConsentRepository
import com.example.batteryalarm.ui.privacy.PrivacySheetMode
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Presentation state holder for onboarding and privacy settings. */
class PrivacyFeature @Inject constructor(
    private val repository: PrivacyConsentRepository,
) {
    private val _state = MutableStateFlow(PrivacyFeatureState())
    val state: StateFlow<PrivacyFeatureState> = _state.asStateFlow()

    private var onboardingDismissedThisSession = false

    fun observe(scope: CoroutineScope) {
        scope.launch {
            repository.consent
                .catch { if (it is CancellationException) throw it }
                .collect { consent ->
                    _state.update { current ->
                        val hasCurrentDecision = !consent.needsDecision
                        val showOnboarding = consent.needsDecision &&
                            !onboardingDismissedThisSession &&
                            current.privacySheetMode != PrivacySheetMode.SETTINGS
                        current.copy(
                            analyticsEnabled = consent.analyticsEnabled,
                            crashlyticsEnabled = consent.crashlyticsEnabled,
                            decisionRecorded = hasCurrentDecision,
                            privacySheetMode = when {
                                hasCurrentDecision &&
                                    current.privacySheetMode == PrivacySheetMode.ONBOARDING -> null
                                showOnboarding -> PrivacySheetMode.ONBOARDING
                                else -> current.privacySheetMode
                            },
                        )
                    }
                }
        }
    }

    fun openSettings() {
        _state.update { it.copy(privacySheetMode = PrivacySheetMode.SETTINGS) }
    }

    fun dismissSheet() {
        val current = _state.value
        if (current.privacySheetMode == PrivacySheetMode.ONBOARDING && !current.decisionRecorded) {
            onboardingDismissedThisSession = true
        }
        _state.update { it.copy(privacySheetMode = null) }
    }

    fun allowBoth(scope: CoroutineScope) = persist(scope, analyticsEnabled = true, crashlyticsEnabled = true)

    fun declineBoth(scope: CoroutineScope) = persist(scope, analyticsEnabled = false, crashlyticsEnabled = false)

    fun saveCustom(scope: CoroutineScope, analyticsEnabled: Boolean, crashlyticsEnabled: Boolean) =
        persist(scope, analyticsEnabled, crashlyticsEnabled)

    fun changeAnalytics(scope: CoroutineScope, enabled: Boolean) =
        persist(scope, analyticsEnabled = enabled, crashlyticsEnabled = _state.value.crashlyticsEnabled)

    fun changeCrashlytics(scope: CoroutineScope, enabled: Boolean) =
        persist(scope, analyticsEnabled = _state.value.analyticsEnabled, crashlyticsEnabled = enabled)

    private fun persist(scope: CoroutineScope, analyticsEnabled: Boolean, crashlyticsEnabled: Boolean) {
        scope.launch {
            repository.setConsent(
                analyticsEnabled = analyticsEnabled,
                crashlyticsEnabled = crashlyticsEnabled,
            )
        }
    }
}

data class PrivacyFeatureState(
    val analyticsEnabled: Boolean = false,
    val crashlyticsEnabled: Boolean = false,
    val decisionRecorded: Boolean = false,
    val privacySheetMode: PrivacySheetMode? = null,
)
