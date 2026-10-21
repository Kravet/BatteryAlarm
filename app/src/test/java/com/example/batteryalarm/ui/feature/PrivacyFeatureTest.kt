package com.example.batteryalarm.ui.feature

import com.example.batteryalarm.domain.privacy.PrivacyConsent
import com.example.batteryalarm.privacy.InMemoryPrivacyConsentRepository
import com.example.batteryalarm.ui.privacy.PrivacySheetMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PrivacyFeatureTest {
    private fun decided(
        analytics: Boolean = false,
        crashlytics: Boolean = false,
    ) = PrivacyConsent(
        analyticsEnabled = analytics,
        crashlyticsEnabled = crashlytics,
        decisionRecorded = true,
        consentTextVersion = PrivacyConsent.CONSENT_TEXT_VERSION,
    )

    @Test fun `missing decision displays onboarding`() = runTest {
        val feature = PrivacyFeature(InMemoryPrivacyConsentRepository())
        feature.observe(backgroundScope)
        runCurrent()
        assertEquals(PrivacySheetMode.ONBOARDING, feature.state.value.privacySheetMode)
        assertFalse(feature.state.value.decisionRecorded)
    }

    @Test fun `current consent keeps sheet hidden and mirrors preferences`() = runTest {
        val feature = PrivacyFeature(InMemoryPrivacyConsentRepository(decided(analytics = true)))
        feature.observe(backgroundScope)
        runCurrent()
        assertNull(feature.state.value.privacySheetMode)
        assertTrue(feature.state.value.analyticsEnabled)
        assertFalse(feature.state.value.crashlyticsEnabled)
        assertTrue(feature.state.value.decisionRecorded)
    }

    @Test fun `outdated consent version requires renewed decision`() = runTest {
        val stale = decided(analytics = true, crashlytics = true).copy(consentTextVersion = "1")
        val feature = PrivacyFeature(InMemoryPrivacyConsentRepository(stale))
        feature.observe(backgroundScope)
        runCurrent()
        assertEquals(PrivacySheetMode.ONBOARDING, feature.state.value.privacySheetMode)
        assertFalse(feature.state.value.decisionRecorded)
    }

    @Test fun `dismiss without decision hides onboarding for the same feature session`() = runTest {
        val repo = InMemoryPrivacyConsentRepository()
        val feature = PrivacyFeature(repo)
        feature.observe(backgroundScope)
        runCurrent()
        feature.dismissSheet()
        feature.observe(backgroundScope)
        runCurrent()
        assertNull(feature.state.value.privacySheetMode)
        assertFalse(repo.current.decisionRecorded)
    }

    @Test fun `settings mode is not replaced by onboarding on another observation`() = runTest {
        val feature = PrivacyFeature(InMemoryPrivacyConsentRepository())
        feature.openSettings()
        feature.observe(backgroundScope)
        runCurrent()
        assertEquals(PrivacySheetMode.SETTINGS, feature.state.value.privacySheetMode)
    }

    @Test fun `allow both records consent and closes onboarding`() = runTest {
        val repo = InMemoryPrivacyConsentRepository()
        val feature = PrivacyFeature(repo)
        feature.observe(backgroundScope)
        runCurrent()
        feature.allowBoth(this)
        advanceUntilIdle()
        assertTrue(repo.current.analyticsEnabled)
        assertTrue(repo.current.crashlyticsEnabled)
        assertTrue(repo.current.decisionRecorded)
        assertNull(feature.state.value.privacySheetMode)
    }

    @Test fun `decline both records disabled consent`() = runTest {
        val repo = InMemoryPrivacyConsentRepository()
        val feature = PrivacyFeature(repo)
        feature.observe(backgroundScope)
        runCurrent()
        feature.declineBoth(this)
        advanceUntilIdle()
        assertFalse(repo.current.analyticsEnabled)
        assertFalse(repo.current.crashlyticsEnabled)
        assertTrue(repo.current.decisionRecorded)
    }

    @Test fun `custom choices persist exactly`() = runTest {
        val repo = InMemoryPrivacyConsentRepository()
        val feature = PrivacyFeature(repo)
        feature.observe(backgroundScope)
        runCurrent()
        feature.saveCustom(this, analyticsEnabled = false, crashlyticsEnabled = true)
        advanceUntilIdle()
        assertFalse(repo.current.analyticsEnabled)
        assertTrue(repo.current.crashlyticsEnabled)
    }

    @Test fun `analytics toggle preserves previously stored Crashlytics choice`() = runTest {
        val repo = InMemoryPrivacyConsentRepository(decided(crashlytics = true))
        val feature = PrivacyFeature(repo)
        feature.observe(backgroundScope)
        runCurrent()
        feature.changeAnalytics(this, true)
        advanceUntilIdle()
        assertTrue(repo.current.analyticsEnabled)
        assertTrue(repo.current.crashlyticsEnabled)
    }

    @Test fun `Crashlytics toggle preserves previously stored Analytics choice`() = runTest {
        val repo = InMemoryPrivacyConsentRepository(decided(analytics = true))
        val feature = PrivacyFeature(repo)
        feature.observe(backgroundScope)
        runCurrent()
        feature.changeCrashlytics(this, true)
        advanceUntilIdle()
        assertTrue(repo.current.analyticsEnabled)
        assertTrue(repo.current.crashlyticsEnabled)
    }

    @Test fun `rapid consecutive consent toggles persist both choices without losing first`() = runTest {
        val repo = InMemoryPrivacyConsentRepository(decided())
        val feature = PrivacyFeature(repo)
        feature.observe(backgroundScope)
        runCurrent()

        // Reproduces two UI callbacks before the Flow emits the first write.
        feature.changeAnalytics(this, true)
        feature.changeCrashlytics(this, true)
        advanceUntilIdle()

        assertTrue("First choice must not be lost", repo.current.analyticsEnabled)
        assertTrue(repo.current.crashlyticsEnabled)
    }

    @Test fun `saving explicit custom choice after toggle wins deterministically`() = runTest {
        val repo = InMemoryPrivacyConsentRepository(decided())
        val feature = PrivacyFeature(repo)
        feature.observe(backgroundScope)
        runCurrent()

        feature.changeAnalytics(this, true)
        feature.saveCustom(this, analyticsEnabled = false, crashlyticsEnabled = true)
        advanceUntilIdle()

        assertFalse(repo.current.analyticsEnabled)
        assertTrue(repo.current.crashlyticsEnabled)
    }
}
