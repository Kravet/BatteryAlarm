package com.example.batteryalarm.ui.feature

import com.example.batteryalarm.analytics.FakeAppAnalytics
import com.example.batteryalarm.domain.AlarmStartReason
import com.example.batteryalarm.domain.AlarmState
import com.example.batteryalarm.domain.update.AppUpdate
import com.example.batteryalarm.domain.update.AppUpdateChecker
import com.example.batteryalarm.domain.update.UpdateCheckResult
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

    private fun checker(result: UpdateCheckResult): AppUpdateChecker = AppUpdateChecker { result }

    private fun feature(
        store: FeatureUpdateStore = FeatureUpdateStore(),
        result: UpdateCheckResult = UpdateCheckResult.Available(
            AppUpdate("1.1.0", "Release notes", releaseUrl),
        ),
        controller: FeatureAlarmController = FeatureAlarmController(),
        opener: FeatureUpdateOpener = FeatureUpdateOpener(),
        analytics: FakeAppAnalytics = FakeAppAnalytics(),
    ) = UpdateFeature(checker(result), store, opener, analytics, controller)

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
    }

    @Test fun `disabled checker never surfaces an update`() = runTest {
        val analytics = FakeAppAnalytics()
        val feature = feature(result = UpdateCheckResult.Disabled, analytics = analytics)
        feature.checkOnStart(this)
        advanceUntilIdle()
        assertNull(feature.state.value.pendingUpdate)
        assertTrue(analytics.updateAvailableShown.isEmpty())
    }

    @Test fun `same installed version does not surface an update`() = runTest {
        val feature = feature(result = UpdateCheckResult.UpToDate)
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
        assertTrue(analytics.updateAvailableShown.isEmpty())
    }

    @Test fun `checker result for a previously persisted update is presented`() = runTest {
        val feature = feature(
            result = UpdateCheckResult.Available(AppUpdate("1.1.0", "Previously found", releaseUrl)),
        )
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
        val feature = feature(result = UpdateCheckResult.Disabled, opener = opener, analytics = analytics)
        feature.openPending(this)
        advanceUntilIdle()
        assertTrue(opener.opened.isEmpty())
        assertTrue(analytics.updateClicked.isEmpty())
    }

    @Test fun `unavailable release leaves update hidden without analytics`() = runTest {
        val analytics = FakeAppAnalytics()
        val feature = feature(result = UpdateCheckResult.NoUsableRelease, analytics = analytics)
        feature.checkOnStart(this)
        advanceUntilIdle()
        assertNull(feature.state.value.pendingUpdate)
        assertTrue(analytics.updateAvailableShown.isEmpty())
    }
}
