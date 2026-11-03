package com.example.batteryalarm.domain.privacy

import kotlinx.coroutines.flow.Flow

interface PrivacyConsentRepository {
    val consent: Flow<PrivacyConsent>

    suspend fun setConsent(
        analyticsEnabled: Boolean,
        crashlyticsEnabled: Boolean,
        decidedAtEpochMs: Long = System.currentTimeMillis(),
        consentTextVersion: String = PrivacyConsent.CONSENT_TEXT_VERSION,
    )
}
