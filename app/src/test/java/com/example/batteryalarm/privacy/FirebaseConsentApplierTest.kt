package com.example.batteryalarm.privacy

import org.junit.Assert.assertEquals
import org.junit.Test

class FirebaseConsentApplierTest {
    @Test
    fun `off off disables both and deletes unsent crash reports`() {
        val analytics = FakeAnalyticsCollectionController()
        val crashlytics = FakeCrashlyticsCollectionController()
        val applier = FirebaseConsentApplier(analytics, crashlytics)

        applier.apply(analyticsEnabled = false, crashlyticsEnabled = false)

        assertEquals(listOf(false), analytics.enabledHistory)
        assertEquals(listOf(false), crashlytics.enabledHistory)
        assertEquals(1, crashlytics.deleteUnsentReportsCount)
    }

    @Test
    fun `on off enables analytics only and deletes unsent crash reports`() {
        val analytics = FakeAnalyticsCollectionController()
        val crashlytics = FakeCrashlyticsCollectionController()
        val applier = FirebaseConsentApplier(analytics, crashlytics)

        applier.apply(analyticsEnabled = true, crashlyticsEnabled = false)

        assertEquals(listOf(true), analytics.enabledHistory)
        assertEquals(listOf(false), crashlytics.enabledHistory)
        assertEquals(1, crashlytics.deleteUnsentReportsCount)
    }

    @Test
    fun `off on enables crashlytics only without deleting unsent reports`() {
        val analytics = FakeAnalyticsCollectionController()
        val crashlytics = FakeCrashlyticsCollectionController()
        val applier = FirebaseConsentApplier(analytics, crashlytics)

        applier.apply(analyticsEnabled = false, crashlyticsEnabled = true)

        assertEquals(listOf(false), analytics.enabledHistory)
        assertEquals(listOf(true), crashlytics.enabledHistory)
        assertEquals(0, crashlytics.deleteUnsentReportsCount)
    }

    @Test
    fun `on on enables both without deleting unsent reports`() {
        val analytics = FakeAnalyticsCollectionController()
        val crashlytics = FakeCrashlyticsCollectionController()
        val applier = FirebaseConsentApplier(analytics, crashlytics)

        applier.apply(analyticsEnabled = true, crashlyticsEnabled = true)

        assertEquals(listOf(true), analytics.enabledHistory)
        assertEquals(listOf(true), crashlytics.enabledHistory)
        assertEquals(0, crashlytics.deleteUnsentReportsCount)
    }
}
