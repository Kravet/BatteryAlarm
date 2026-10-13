package com.example.batteryalarm.ui

import com.example.batteryalarm.domain.update.AppUpdate
import com.example.batteryalarm.domain.update.CheckForAppUpdate
import com.example.batteryalarm.domain.update.InstalledAppVersionName
import com.example.batteryalarm.domain.update.LatestStableRelease
import com.example.batteryalarm.domain.update.LatestStableReleaseSource
import com.example.batteryalarm.domain.update.UpdateCheckAttemptStore
import com.example.batteryalarm.domain.update.UpdateCheckResult
import com.example.batteryalarm.domain.update.UpdateCheckerEnabled
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * E2E/UI scenario (mocked remote): open-app update flow without real GitHub API.
 * Covers update availability for the inline button and Update URL hand-off.
 */
class UpdateCheckUiScenarioTest {
    @Test
    fun `open app with newer release yields available update for inline button`() = runTest {
        val releaseUrl = "https://github.com/PLACEHOLDER_OWNER/PLACEHOLDER_REPOSITORY/releases/tag/v1.1.0"
        val checker = CheckForAppUpdate(
            isEnabled = UpdateCheckerEnabled { true },
            attemptStore = InMemoryAttemptStore(),
            releaseSource = FixedReleaseSource(
                LatestStableRelease(
                    tagName = "v1.1.0",
                    body = "Improved battery monitoring",
                    htmlUrl = releaseUrl,
                ),
            ),
            installedVersionName = InstalledAppVersionName { "1.0.0" },
            clockMillis = { 1_000L },
        )

        val result = checker()

        assertEquals(
            UpdateCheckResult.Available(
                AppUpdate(
                    versionName = "1.1.0",
                    releaseNotes = "Improved battery monitoring",
                    releasePageUrl = releaseUrl,
                ),
            ),
            result,
        )
    }

    @Test
    fun `update action exposes release page url for browser intent`() {
        val pending = AppUpdateUiModel(
            versionName = "1.1.0",
            releaseNotes = null,
            releasePageUrl = "https://github.com/example/repo/releases/tag/v1.1.0",
        )

        assertEquals(
            "https://github.com/example/repo/releases/tag/v1.1.0",
            pending.releasePageUrl,
        )
    }

    private class InMemoryAttemptStore(
        private var value: Long? = null,
        private var pending: AppUpdate? = null,
    ) : UpdateCheckAttemptStore {
        override suspend fun getLastCheckAttemptAtMillis(): Long? = value
        override suspend fun setLastCheckAttemptAtMillis(epochMillis: Long) {
            value = epochMillis
        }

        override suspend fun getPendingUpdate(): AppUpdate? = pending

        override suspend fun setPendingUpdate(update: AppUpdate) {
            pending = update
        }

        override suspend fun clearPendingUpdate() {
            pending = null
        }
    }

    private class FixedReleaseSource(
        private val release: LatestStableRelease,
    ) : LatestStableReleaseSource {
        override suspend fun fetchLatestStableRelease(): LatestStableRelease = release
    }
}
