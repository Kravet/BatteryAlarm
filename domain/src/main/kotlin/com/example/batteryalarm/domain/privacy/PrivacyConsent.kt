package com.example.batteryalarm.domain.privacy

data class PrivacyConsent(
    val analyticsEnabled: Boolean = false,
    val crashlyticsEnabled: Boolean = false,
    val decisionRecorded: Boolean = false,
    val decidedAtEpochMs: Long? = null,
    val consentTextVersion: String? = null,
) {
    val needsDecision: Boolean
        get() = !decisionRecorded || consentTextVersion != CONSENT_TEXT_VERSION

    val effectiveAnalyticsEnabled: Boolean
        get() = analyticsEnabled && !needsDecision

    val effectiveCrashlyticsEnabled: Boolean
        get() = crashlyticsEnabled && !needsDecision

    companion object {
        const val CONSENT_TEXT_VERSION = "2"

        val Default = PrivacyConsent()
    }
}
