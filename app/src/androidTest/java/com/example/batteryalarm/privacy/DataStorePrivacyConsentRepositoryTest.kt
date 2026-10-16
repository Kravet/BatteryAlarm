package com.example.batteryalarm.privacy

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.batteryalarm.domain.privacy.PrivacyConsent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DataStorePrivacyConsentRepositoryTest {
    private lateinit var repository: DataStorePrivacyConsentRepository

    @Before
    fun setUp() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        repository = DataStorePrivacyConsentRepository(context)
        repository.clearConsent()
    }

    @Test
    fun defaults_keep_collection_off_until_decision() = runBlocking {
        val consent = repository.consent.first()

        assertFalse(consent.analyticsEnabled)
        assertFalse(consent.crashlyticsEnabled)
        assertFalse(consent.decisionRecorded)
        assertNull(consent.decidedAtEpochMs)
        assertNull(consent.consentTextVersion)
    }

    @Test
    fun setConsent_persists_independent_flags_and_metadata() = runBlocking {
        repository.setConsent(
            analyticsEnabled = true,
            crashlyticsEnabled = false,
            decidedAtEpochMs = 123L,
            consentTextVersion = PrivacyConsent.CONSENT_TEXT_VERSION,
        )

        val consent = repository.consent.first()
        assertTrue(consent.analyticsEnabled)
        assertFalse(consent.crashlyticsEnabled)
        assertTrue(consent.decisionRecorded)
        assertEquals(123L, consent.decidedAtEpochMs)
        assertEquals(PrivacyConsent.CONSENT_TEXT_VERSION, consent.consentTextVersion)
    }

    @Test
    fun setConsent_can_withdraw_previously_granted_opt_in() = runBlocking {
        repository.setConsent(analyticsEnabled = true, crashlyticsEnabled = true)
        repository.setConsent(analyticsEnabled = false, crashlyticsEnabled = false)

        val consent = repository.consent.first()
        assertFalse(consent.analyticsEnabled)
        assertFalse(consent.crashlyticsEnabled)
        assertTrue(consent.decisionRecorded)
    }

    @Test
    fun clearConsent_resets_to_undecided_defaults() = runBlocking {
        repository.setConsent(analyticsEnabled = true, crashlyticsEnabled = true)
        repository.clearConsent()

        val consent = repository.consent.first()
        assertFalse(consent.analyticsEnabled)
        assertFalse(consent.crashlyticsEnabled)
        assertFalse(consent.decisionRecorded)
        assertNull(consent.decidedAtEpochMs)
    }
}
