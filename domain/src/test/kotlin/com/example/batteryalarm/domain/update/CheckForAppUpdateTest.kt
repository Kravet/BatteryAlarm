package com.example.batteryalarm.domain.update

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CheckForAppUpdateTest {
    @Test
    fun `disabled checker does not call remote or update timestamp`() = runTest {
        val store = FakeUpdateCheckAttemptStore()
        val remote = FakeLatestStableReleaseSource()
        val checker = CheckForAppUpdate(
            isEnabled = UpdateCheckerEnabled { false },
            attemptStore = store,
            releaseSource = remote,
            installedVersionName = InstalledAppVersionName { "1.0.0" },
            clockMillis = { 1_000_000L },
        )

        val result = checker()

        assertEquals(UpdateCheckResult.Disabled, result)
        assertFalse(remote.wasCalled)
        assertEquals(null, store.lastCheckAttemptAtMillis)
    }

    @Test
    fun `skips remote when last attempt was within 24 hours`() = runTest {
        val now = 10_000_000L
        val store = FakeUpdateCheckAttemptStore(
            lastCheckAttemptAtMillis = now - CheckForAppUpdate.DEFAULT_INTERVAL_MILLIS + 1,
        )
        val remote = FakeLatestStableReleaseSource(
            release = newerRelease(),
        )
        val checker = CheckForAppUpdate(
            isEnabled = UpdateCheckerEnabled { true },
            attemptStore = store,
            releaseSource = remote,
            installedVersionName = InstalledAppVersionName { "1.0.0" },
            clockMillis = { now },
        )

        val result = checker()

        assertEquals(UpdateCheckResult.SkippedWithinInterval, result)
        assertFalse(remote.wasCalled)
    }

    @Test
    fun `within interval returns persisted pending update without calling remote`() = runTest {
        val now = 10_000_000L
        val pending = AppUpdate(
            versionName = "1.1.0",
            releaseNotes = "Notes",
            releasePageUrl = "https://github.com/example/repo/releases/tag/v1.1.0",
        )
        val store = FakeUpdateCheckAttemptStore(
            lastCheckAttemptAtMillis = now - 1,
            pendingUpdate = pending,
        )
        val remote = FakeLatestStableReleaseSource(release = newerRelease())
        val checker = CheckForAppUpdate(
            isEnabled = UpdateCheckerEnabled { true },
            attemptStore = store,
            releaseSource = remote,
            installedVersionName = InstalledAppVersionName { "1.0.0" },
            clockMillis = { now },
        )

        assertEquals(UpdateCheckResult.Available(pending), checker())
        assertFalse(remote.wasCalled)
    }

    @Test
    fun `disallowed release url returns no usable release`() = runTest {
        val store = FakeUpdateCheckAttemptStore()
        val remote = FakeLatestStableReleaseSource(release = newerRelease())
        val checker = CheckForAppUpdate(
            isEnabled = UpdateCheckerEnabled { true },
            attemptStore = store,
            releaseSource = remote,
            installedVersionName = InstalledAppVersionName { "1.0.0" },
            clockMillis = { 1L },
            isAllowedReleasePageUrl = { false },
        )

        assertEquals(UpdateCheckResult.NoUsableRelease, checker())
        assertEquals(null, store.pendingUpdate)
    }

    @Test
    fun `calls remote after 24 hours and records attempt timestamp`() = runTest {
        val now = 10_000_000L
        val store = FakeUpdateCheckAttemptStore(
            lastCheckAttemptAtMillis = now - CheckForAppUpdate.DEFAULT_INTERVAL_MILLIS,
        )
        val remote = FakeLatestStableReleaseSource(release = newerRelease())
        val checker = CheckForAppUpdate(
            isEnabled = UpdateCheckerEnabled { true },
            attemptStore = store,
            releaseSource = remote,
            installedVersionName = InstalledAppVersionName { "1.0.0" },
            clockMillis = { now },
        )

        val result = checker()

        assertTrue(remote.wasCalled)
        assertEquals(now, store.lastCheckAttemptAtMillis)
        assertEquals(
            UpdateCheckResult.Available(
                AppUpdate(
                    versionName = "1.1.0",
                    releaseNotes = "Notes",
                    releasePageUrl = "https://github.com/example/repo/releases/tag/v1.1.0",
                ),
            ),
            result,
        )
    }

    @Test
    fun `first run with no prior attempt fetches remote`() = runTest {
        val now = 5_000L
        val store = FakeUpdateCheckAttemptStore()
        val remote = FakeLatestStableReleaseSource(release = newerRelease())
        val checker = CheckForAppUpdate(
            isEnabled = UpdateCheckerEnabled { true },
            attemptStore = store,
            releaseSource = remote,
            installedVersionName = InstalledAppVersionName { "1.0.0" },
            clockMillis = { now },
        )

        checker()

        assertTrue(remote.wasCalled)
        assertEquals(now, store.lastCheckAttemptAtMillis)
    }

    @Test
    fun `same version returns up to date`() = runTest {
        val store = FakeUpdateCheckAttemptStore()
        val remote = FakeLatestStableReleaseSource(
            release = LatestStableRelease(
                tagName = "v1.0.0",
                body = null,
                htmlUrl = "https://github.com/example/repo/releases/tag/v1.0.0",
            ),
        )
        val checker = CheckForAppUpdate(
            isEnabled = UpdateCheckerEnabled { true },
            attemptStore = store,
            releaseSource = remote,
            installedVersionName = InstalledAppVersionName { "1.0.0" },
            clockMillis = { 1L },
        )

        assertEquals(UpdateCheckResult.UpToDate, checker())
    }

    @Test
    fun `older remote version returns up to date`() = runTest {
        val store = FakeUpdateCheckAttemptStore()
        val remote = FakeLatestStableReleaseSource(
            release = LatestStableRelease(
                tagName = "v1.0.0",
                body = null,
                htmlUrl = "https://github.com/example/repo/releases/tag/v1.0.0",
            ),
        )
        val checker = CheckForAppUpdate(
            isEnabled = UpdateCheckerEnabled { true },
            attemptStore = store,
            releaseSource = remote,
            installedVersionName = InstalledAppVersionName { "1.10.0" },
            clockMillis = { 1L },
        )

        assertEquals(UpdateCheckResult.UpToDate, checker())
    }

    @Test
    fun `newer remote with v prefix is available using SemVer core`() = runTest {
        val store = FakeUpdateCheckAttemptStore()
        val remote = FakeLatestStableReleaseSource(
            release = LatestStableRelease(
                tagName = "v1.10.0",
                body = "  ",
                htmlUrl = "https://github.com/example/repo/releases/tag/v1.10.0",
            ),
        )
        val checker = CheckForAppUpdate(
            isEnabled = UpdateCheckerEnabled { true },
            attemptStore = store,
            releaseSource = remote,
            installedVersionName = InstalledAppVersionName { "1.9.0" },
            clockMillis = { 1L },
        )

        val result = checker()
        assertEquals(
            UpdateCheckResult.Available(
                AppUpdate(
                    versionName = "1.10.0",
                    releaseNotes = null,
                    releasePageUrl = "https://github.com/example/repo/releases/tag/v1.10.0",
                ),
            ),
            result,
        )
    }

    @Test
    fun `api failure still records attempt and returns no usable release`() = runTest {
        val now = 42L
        val store = FakeUpdateCheckAttemptStore()
        val remote = FakeLatestStableReleaseSource(release = null)
        val checker = CheckForAppUpdate(
            isEnabled = UpdateCheckerEnabled { true },
            attemptStore = store,
            releaseSource = remote,
            installedVersionName = InstalledAppVersionName { "1.0.0" },
            clockMillis = { now },
        )

        assertEquals(UpdateCheckResult.NoUsableRelease, checker())
        assertEquals(now, store.lastCheckAttemptAtMillis)
    }

    @Test
    fun `remote exception is treated as failure and still records attempt`() = runTest {
        val now = 99L
        val store = FakeUpdateCheckAttemptStore()
        val remote = FakeLatestStableReleaseSource(throwOnFetch = IllegalStateException("boom"))
        val checker = CheckForAppUpdate(
            isEnabled = UpdateCheckerEnabled { true },
            attemptStore = store,
            releaseSource = remote,
            installedVersionName = InstalledAppVersionName { "1.0.0" },
            clockMillis = { now },
        )

        assertEquals(UpdateCheckResult.NoUsableRelease, checker())
        assertEquals(now, store.lastCheckAttemptAtMillis)
    }

    @Test
    fun `cancelled release request propagates cancellation without throttling next attempt`() = runTest {
        val store = FakeUpdateCheckAttemptStore()
        val remote = FakeLatestStableReleaseSource(
            throwOnFetch = CancellationException("cancelled"),
        )
        val checker = CheckForAppUpdate(
            isEnabled = UpdateCheckerEnabled { true },
            attemptStore = store,
            releaseSource = remote,
            installedVersionName = InstalledAppVersionName { "1.0.0" },
            clockMillis = { 100L },
        )

        try {
            checker()
            org.junit.Assert.fail("Cancellation must propagate")
        } catch (exception: CancellationException) {
            assertEquals("cancelled", exception.message)
        }
        assertTrue(remote.wasCalled)
        assertEquals(null, store.lastCheckAttemptAtMillis)
    }

    @Test
    fun `malformed remote tag returns no usable release`() = runTest {
        val store = FakeUpdateCheckAttemptStore()
        val remote = FakeLatestStableReleaseSource(
            release = LatestStableRelease(
                tagName = "v1.0.0-beta.1",
                body = null,
                htmlUrl = "https://github.com/example/repo/releases/tag/v1.0.0-beta.1",
            ),
        )
        val checker = CheckForAppUpdate(
            isEnabled = UpdateCheckerEnabled { true },
            attemptStore = store,
            releaseSource = remote,
            installedVersionName = InstalledAppVersionName { "1.0.0" },
            clockMillis = { 1L },
        )

        assertEquals(UpdateCheckResult.NoUsableRelease, checker())
    }

    @Test
    fun `empty release url returns no usable release`() = runTest {
        val store = FakeUpdateCheckAttemptStore()
        val remote = FakeLatestStableReleaseSource(
            release = LatestStableRelease(
                tagName = "v2.0.0",
                body = null,
                htmlUrl = "  ",
            ),
        )
        val checker = CheckForAppUpdate(
            isEnabled = UpdateCheckerEnabled { true },
            attemptStore = store,
            releaseSource = remote,
            installedVersionName = InstalledAppVersionName { "1.0.0" },
            clockMillis = { 1L },
        )

        assertEquals(UpdateCheckResult.NoUsableRelease, checker())
    }

    private fun newerRelease() = LatestStableRelease(
        tagName = "v1.1.0",
        body = "Notes",
        htmlUrl = "https://github.com/example/repo/releases/tag/v1.1.0",
    )

    private class FakeUpdateCheckAttemptStore(
        var lastCheckAttemptAtMillis: Long? = null,
        var pendingUpdate: AppUpdate? = null,
    ) : UpdateCheckAttemptStore {
        override suspend fun getLastCheckAttemptAtMillis(): Long? = lastCheckAttemptAtMillis

        override suspend fun setLastCheckAttemptAtMillis(epochMillis: Long) {
            lastCheckAttemptAtMillis = epochMillis
        }

        override suspend fun getPendingUpdate(): AppUpdate? = pendingUpdate

        override suspend fun setPendingUpdate(update: AppUpdate) {
            pendingUpdate = update
        }

        override suspend fun clearPendingUpdate() {
            pendingUpdate = null
        }
    }

    private class FakeLatestStableReleaseSource(
        private val release: LatestStableRelease? = null,
        private val throwOnFetch: Throwable? = null,
    ) : LatestStableReleaseSource {
        var wasCalled: Boolean = false
            private set

        override suspend fun fetchLatestStableRelease(): LatestStableRelease? {
            wasCalled = true
            throwOnFetch?.let { throw it }
            return release
        }
    }
}
