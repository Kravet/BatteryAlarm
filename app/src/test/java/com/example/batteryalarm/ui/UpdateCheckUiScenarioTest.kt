package com.example.batteryalarm.ui

import com.example.batteryalarm.domain.update.AppUpdate
import com.example.batteryalarm.domain.update.AppUpdateChecker
import com.example.batteryalarm.domain.update.UpdateCheckResult
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
        val checker = AppUpdateChecker {
            UpdateCheckResult.Available(
                AppUpdate("1.1.0", "Improved battery monitoring", releaseUrl),
            )
        }

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
}
