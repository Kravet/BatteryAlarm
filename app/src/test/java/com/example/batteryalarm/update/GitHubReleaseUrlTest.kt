package com.example.batteryalarm.update

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GitHubReleaseUrlTest {
    @Test
    fun `allows https github release urls for configured repo`() {
        assertTrue(
            GitHubReleaseUrl.isAllowedReleasePageUrl(
                url = "https://github.com/Kravet/battery-alarm-releases/releases/tag/v1.1.0",
                owner = "Kravet",
                repo = "battery-alarm-releases",
            ),
        )
    }

    @Test
    fun `rejects non https and non github hosts`() {
        assertFalse(
            GitHubReleaseUrl.isAllowedReleasePageUrl(
                url = "http://github.com/Kravet/battery-alarm-releases/releases/tag/v1.1.0",
                owner = "Kravet",
                repo = "battery-alarm-releases",
            ),
        )
        assertFalse(
            GitHubReleaseUrl.isAllowedReleasePageUrl(
                url = "https://evil.example/fake-release",
                owner = "Kravet",
                repo = "battery-alarm-releases",
            ),
        )
    }

    @Test
    fun `rejects urls for a different repository`() {
        assertFalse(
            GitHubReleaseUrl.isAllowedReleasePageUrl(
                url = "https://github.com/other/repo/releases/tag/v1.1.0",
                owner = "Kravet",
                repo = "battery-alarm-releases",
            ),
        )
    }
}
