package com.example.batteryalarm.ui.feature

import com.example.batteryalarm.analytics.FakeAppAnalytics
import com.example.batteryalarm.domain.AlarmStartReason
import com.example.batteryalarm.domain.AlarmState
import com.example.batteryalarm.domain.update.AppUpdate
import com.example.batteryalarm.domain.update.CheckForAppUpdate
import com.example.batteryalarm.domain.update.InstalledAppVersionName
import com.example.batteryalarm.domain.update.LatestStableRelease
import com.example.batteryalarm.domain.update.LatestStableReleaseSource
import com.example.batteryalarm.domain.update.UpdateCheckerEnabled
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UpdateFeatureTest {
    private val releaseUrl = "https://github.com/example/releases/tag/v1.1.0"

    private fun checker(
        store: FeatureUpdateStore,
        enabled: Boolean = true,
        release: LatestStableRelease? = LatestStableRelease("v1.1.0", "Release notes", releaseUrl),
    ): CheckForAppUpdate = CheckForAppUpdate(
        isEnabled = UpdateCheckerEnabled { enabled },
        attemptStore = store,
        releaseSource = object : LatestStableReleaseSource {
            override suspend fun fetchLatestStableRelease(): LatestStableRelease? = release
        },
        installedVersionName = InstalledAppVersionName { "1.0.0" },
        clockMillis = { 1_000L },
    )

    private fun feature(
        store: FeatureUpdateStore = FeatureUpdateStore(),
        enabled: Boolean = true,
        release: LatestStableRelease? = LatestStableRelease("v1.1.0", "Release notes", releaseUrl),
        controller: FeatureAlarmController = FeatureAlarmController(),
        opener: FeatureUpdateOpener = FeatureUpdateOpener(),
        analytics: FakeAppAnalytics = FakeAppAnalytics(),
    ) = UpdateFeature(checker(store, enabled, release), store, opener, analytics, controller)

    @Test fun `available update maps domain result to presentation and analytics`() = runTest {
        val analytics = FakeAppAnalytics()
        val store = FeatureUpdateStore()
        val feature = feature(store = store, analytics = analytics)
        feature.checkOnStart(this)
        advanceUntilIdle()

        assertEquals("1.1.0", feature.state.value.pendingUpdate?.versionName)
        assertEquals("Release notes", feature.state.value.pendingUpdate?.releaseNotes)
        assertEquals(releaseUrl, feature.state.value.pendingUpdate?.releasePageUrl)
        assertEquals(listOf("1.1.0"), analytics.updateAvailableShown)
        assertEquals("1.1.0", store.pending?.versionName)
    }

    @Test fun `disabled checker never surfaces an update`() = runTest {
        val analytics = FakeAppAnalytics()
        val feature = feature(enabled = false, analytics = analytics)
        feature.checkOnStart(this)
        advanceUntilIdle()
        assertNull(feature.state.value.pendingUpdate)
        assertTrue(analytics.updateAvailableShown.isEmpty())
    }

    @Test fun `same installed version does not surface an update`() = runTest {
        val feature = feature(release = LatestStableRelease("v1.0.0", null, releaseUrl))
        feature.checkOnStart(this)
        advanceUntilIdle()
        assertNull(feature.state.value.pendingUpdate)
    }

    @Test fun `active alarm suppresses update UI but retains update for next launch`() = runTest {
        val store = FeatureUpdateStore()
        val analytics = FakeAppAnalytics()
        val feature = feature(
            store = store, analytics = analytics,
            controller = FeatureAlarmController(AlarmState.Active(AlarmStartReason.SystemLowBattery)),
        )
        feature.checkOnStart(this)
        advanceUntilIdle()

        assertNull(feature.state.value.pendingUpdate)
        assertEquals("1.1.0", store.pending?.versionName)
        assertTrue(analytics.updateAvailableShown.isEmpty())
    }

    @Test fun `pending update survives throttled app launch`() = runTest {
        val store = FeatureUpdateStore(
            checkedAt = 500L,
            pending = AppUpdate("1.1.0", "Previously found", releaseUrl),
        )
        val feature = feature(store = store, release = null)
        feature.checkOnStart(this)
        advanceUntilIdle()
        assertEquals("Previously found", feature.state.value.pendingUpdate?.releaseNotes)
    }

    @Test fun `clicking update opens URL clears persisted pending and records analytics`() = runTest {
        val store = FeatureUpdateStore()
        val opener = FeatureUpdateOpener()
        val analytics = FakeAppAnalytics()
        val feature = feature(store = store, opener = opener, analytics = analytics)
        feature.checkOnStart(this)
        advanceUntilIdle()

        feature.openPending(this)
        advanceUntilIdle()

        assertEquals(listOf(releaseUrl), opener.opened)
        assertEquals(listOf("1.1.0"), analytics.updateClicked)
        assertNull(feature.state.value.pendingUpdate)
        assertNull(store.pending)
    }

    @Test fun `click with no pending update has no side effects`() = runTest {
        val opener = FeatureUpdateOpener()
        val analytics = FakeAppAnalytics()
        val feature = feature(enabled = false, opener = opener, analytics = analytics)
        feature.openPending(this)
        advanceUntilIdle()
        assertTrue(opener.opened.isEmpty())
        assertTrue(analytics.updateClicked.isEmpty())
    }

    @Test fun `unavailable release leaves update hidden without analytics`() = runTest {
        val analytics = FakeAppAnalytics()
        val feature = feature(release = null, analytics = analytics)
        feature.checkOnStart(this)
        advanceUntilIdle()
        assertNull(feature.state.value.pendingUpdate)
        assertTrue(analytics.updateAvailableShown.isEmpty())
    }
}
