package com.example.batteryalarm.privacy

import com.example.batteryalarm.domain.privacy.PrivacyConsent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseConsentCoordinatorTest {
    @Test
    fun `startup with no decision keeps both collections disabled`() = runTest {
        val repository = InMemoryPrivacyConsentRepository()
        val analytics = FakeAnalyticsCollectionController()
        val crashlytics = FakeCrashlyticsCollectionController()
        val coordinator = FirebaseConsentCoordinator(
            repository = repository,
            applier = FirebaseConsentApplier(analytics, crashlytics),
            scope = backgroundScope,
        )

        coordinator.start()
        coordinator.start() // Multiple application starts must not duplicate observation.
        runCurrent()

        assertEquals(listOf(false), analytics.enabledHistory)
        assertEquals(listOf(false), crashlytics.enabledHistory)
        assertEquals(1, crashlytics.deleteUnsentReportsCount)
    }

    @Test
    fun `grant and withdrawal are applied once per effective consent change`() = runTest {
        val repository = InMemoryPrivacyConsentRepository()
        val analytics = FakeAnalyticsCollectionController()
        val crashlytics = FakeCrashlyticsCollectionController()
        val coordinator = FirebaseConsentCoordinator(
            repository = repository,
            applier = FirebaseConsentApplier(analytics, crashlytics),
            scope = backgroundScope,
        )
        coordinator.start()
        runCurrent()

        repository.setConsent(analyticsEnabled = true, crashlyticsEnabled = true)
        runCurrent()

        // A new decision timestamp does not change effective consent.
        repository.setConsent(analyticsEnabled = true, crashlyticsEnabled = true)
        runCurrent()

        repository.setConsent(analyticsEnabled = false, crashlyticsEnabled = false)
        runCurrent()

        assertEquals(listOf(false, true, false), analytics.enabledHistory)
        assertEquals(listOf(false, true, false), crashlytics.enabledHistory)
        assertEquals(2, crashlytics.deleteUnsentReportsCount)
    }

    @Test
    fun `outdated consent text is not treated as permission to collect`() = runTest {
        val repository = InMemoryPrivacyConsentRepository(
            PrivacyConsent(
                analyticsEnabled = true,
                crashlyticsEnabled = true,
                decisionRecorded = true,
                consentTextVersion = "1",
            ),
        )
        val analytics = FakeAnalyticsCollectionController()
        val crashlytics = FakeCrashlyticsCollectionController()
        FirebaseConsentCoordinator(
            repository = repository,
            applier = FirebaseConsentApplier(analytics, crashlytics),
            scope = backgroundScope,
        ).start()
        runCurrent()

        assertEquals(listOf(false), analytics.enabledHistory)
        assertEquals(listOf(false), crashlytics.enabledHistory)
        assertEquals(1, crashlytics.deleteUnsentReportsCount)
    }
}
