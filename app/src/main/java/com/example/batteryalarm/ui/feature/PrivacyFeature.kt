package com.example.batteryalarm.ui.feature

import com.example.batteryalarm.domain.privacy.PrivacyConsent
import com.example.batteryalarm.domain.privacy.PrivacyConsentRepository
import com.example.batteryalarm.ui.privacy.PrivacySheetMode
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Presentation state holder for onboarding and privacy settings. */
class PrivacyFeature @Inject constructor(
    private val repository: PrivacyConsentRepository,
) {
    private val _state = MutableStateFlow(PrivacyFeatureState())
    val state: StateFlow<PrivacyFeatureState> = _state.asStateFlow()

    private var onboardingDismissedThisSession = false
    private val consentWrites = Mutex()

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
        updateConsent(scope) { current -> enabled to current.crashlyticsEnabled }

    fun changeCrashlytics(scope: CoroutineScope, enabled: Boolean) =
        updateConsent(scope) { current -> current.analyticsEnabled to enabled }

    private fun persist(scope: CoroutineScope, analyticsEnabled: Boolean, crashlyticsEnabled: Boolean) =
        updateConsent(scope) { analyticsEnabled to crashlyticsEnabled }

    /**
     * Serialize read-modify-write operations against persisted consent.
     * Reading the UI snapshot here loses the first choice when two toggles
     * are tapped before the repository Flow delivers its next emission.
     */
    private fun updateConsent(
        scope: CoroutineScope,
        choices: (PrivacyConsent) -> Pair<Boolean, Boolean>,
    ) {
        scope.launch {
            consentWrites.withLock {
                val (analytics, crashlytics) = choices(repository.consent.first())
                repository.setConsent(
                    analyticsEnabled = analytics,
                    crashlyticsEnabled = crashlytics,
                )
            }
        }
    }
}

data class PrivacyFeatureState(
    val analyticsEnabled: Boolean = false,
    val crashlyticsEnabled: Boolean = false,
    val decisionRecorded: Boolean = false,
    val privacySheetMode: PrivacySheetMode? = null,
)
