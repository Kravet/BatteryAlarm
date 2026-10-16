package com.example.batteryalarm.domain.privacy

data class PrivacyConsent(
    val analyticsEnabled: Boolean = false,
    val crashlyticsEnabled: Boolean = false,
    val decisionRecorded: Boolean = false,
    val decidedAtEpochMs: Long? = null,
    val consentTextVersion: String? = null,
) {
    companion object {
        const val CONSENT_TEXT_VERSION = "1"

        val Default = PrivacyConsent()
    }
}
