package com.example.batteryalarm.domain.privacy

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PrivacyConsentTest {
    @Test
    fun default_is_opt_out_with_no_decision() {
        val consent = PrivacyConsent.Default

        assertFalse(consent.analyticsEnabled)
        assertFalse(consent.crashlyticsEnabled)
        assertFalse(consent.decisionRecorded)
        assertNull(consent.decidedAtEpochMs)
        assertNull(consent.consentTextVersion)
        assertTrue(consent.needsDecision)
        assertFalse(consent.effectiveAnalyticsEnabled)
        assertFalse(consent.effectiveCrashlyticsEnabled)
        assertEquals("2", PrivacyConsent.CONSENT_TEXT_VERSION)
    }

    @Test
    fun current_version_decision_keeps_opt_in_effective() {
        val consent = PrivacyConsent(
            analyticsEnabled = true,
            crashlyticsEnabled = true,
            decisionRecorded = true,
            consentTextVersion = PrivacyConsent.CONSENT_TEXT_VERSION,
        )

        assertFalse(consent.needsDecision)
        assertTrue(consent.effectiveAnalyticsEnabled)
        assertTrue(consent.effectiveCrashlyticsEnabled)
    }

    @Test
    fun stale_or_missing_text_version_disables_collection_until_new_decision() {
        val stale = PrivacyConsent(
            analyticsEnabled = true,
            crashlyticsEnabled = true,
            decisionRecorded = true,
            consentTextVersion = "1",
        )
        val missingVersion = PrivacyConsent(
            analyticsEnabled = true,
            crashlyticsEnabled = false,
            decisionRecorded = true,
        )

        assertTrue(stale.needsDecision)
        assertFalse(stale.effectiveAnalyticsEnabled)
        assertFalse(stale.effectiveCrashlyticsEnabled)
        assertTrue(missingVersion.needsDecision)
        assertFalse(missingVersion.effectiveAnalyticsEnabled)
        assertFalse(missingVersion.effectiveCrashlyticsEnabled)
    }
}
