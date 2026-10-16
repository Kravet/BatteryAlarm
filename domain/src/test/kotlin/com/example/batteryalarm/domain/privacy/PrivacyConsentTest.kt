package com.example.batteryalarm.domain.privacy

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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
        assertEquals("1", PrivacyConsent.CONSENT_TEXT_VERSION)
    }
}
