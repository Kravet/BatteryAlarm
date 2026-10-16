package com.example.batteryalarm.privacy

import com.example.batteryalarm.domain.privacy.PrivacyConsent
import com.example.batteryalarm.domain.privacy.PrivacyConsentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryPrivacyConsentRepository(
    initial: PrivacyConsent = PrivacyConsent.Default,
) : PrivacyConsentRepository {
    private val _consent = MutableStateFlow(initial)
    override val consent: Flow<PrivacyConsent> = _consent.asStateFlow()

    val current: PrivacyConsent get() = _consent.value

    override suspend fun setConsent(
        analyticsEnabled: Boolean,
        crashlyticsEnabled: Boolean,
        decidedAtEpochMs: Long,
        consentTextVersion: String,
    ) {
        _consent.value = PrivacyConsent(
            analyticsEnabled = analyticsEnabled,
            crashlyticsEnabled = crashlyticsEnabled,
            decisionRecorded = true,
            decidedAtEpochMs = decidedAtEpochMs,
            consentTextVersion = consentTextVersion,
        )
    }
}
