package com.example.batteryalarm.privacy

import com.example.batteryalarm.domain.privacy.PrivacyConsent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryPrivacyConsentRepositoryTest {
    @Test
    fun `defaults keep both services off and decision unrecorded`() {
        val repository = InMemoryPrivacyConsentRepository()

        assertEquals(PrivacyConsent.Default, repository.current)
        assertFalse(repository.current.analyticsEnabled)
        assertFalse(repository.current.crashlyticsEnabled)
        assertFalse(repository.current.decisionRecorded)
    }

    @Test
    fun `allow both persists decision and enables services`() = runTest {
        val repository = InMemoryPrivacyConsentRepository()

        repository.setConsent(analyticsEnabled = true, crashlyticsEnabled = true, decidedAtEpochMs = 42L)

        assertTrue(repository.current.analyticsEnabled)
        assertTrue(repository.current.crashlyticsEnabled)
        assertTrue(repository.current.decisionRecorded)
        assertEquals(42L, repository.current.decidedAtEpochMs)
        assertEquals(PrivacyConsent.CONSENT_TEXT_VERSION, repository.current.consentTextVersion)
    }

    @Test
    fun `decline both persists decision with services disabled`() = runTest {
        val repository = InMemoryPrivacyConsentRepository()

        repository.setConsent(analyticsEnabled = false, crashlyticsEnabled = false)

        assertFalse(repository.current.analyticsEnabled)
        assertFalse(repository.current.crashlyticsEnabled)
        assertTrue(repository.current.decisionRecorded)
    }

    @Test
    fun `customize persists independent choices`() = runTest {
        val repository = InMemoryPrivacyConsentRepository()

        repository.setConsent(analyticsEnabled = true, crashlyticsEnabled = false)

        assertTrue(repository.current.analyticsEnabled)
        assertFalse(repository.current.crashlyticsEnabled)
        assertTrue(repository.current.decisionRecorded)
    }
}
