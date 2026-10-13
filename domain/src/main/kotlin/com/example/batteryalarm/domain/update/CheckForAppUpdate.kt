package com.example.batteryalarm.domain.update

class CheckForAppUpdate(
    private val isEnabled: UpdateCheckerEnabled,
    private val attemptStore: UpdateCheckAttemptStore,
    private val releaseSource: LatestStableReleaseSource,
    private val installedVersionName: InstalledAppVersionName,
    private val clockMillis: () -> Long = { System.currentTimeMillis() },
    private val intervalMillis: Long = DEFAULT_INTERVAL_MILLIS,
    private val isAllowedReleasePageUrl: (String) -> Boolean = { true },
) {
    suspend operator fun invoke(): UpdateCheckResult {
        if (!isEnabled.isEnabled()) {
            return UpdateCheckResult.Disabled
        }

        val now = clockMillis()
        val lastAttempt = attemptStore.getLastCheckAttemptAtMillis()
        if (lastAttempt != null && now - lastAttempt < intervalMillis) {
            return pendingAvailableOrSkipped()
        }

        val release = try {
            releaseSource.fetchLatestStableRelease()
        } catch (_: Exception) {
            null
        }

        attemptStore.setLastCheckAttemptAtMillis(now)

        if (release == null) {
            return UpdateCheckResult.NoUsableRelease
        }

        val remoteVersion = SemVer.parse(release.tagName)
            ?: return UpdateCheckResult.NoUsableRelease
        val installedVersion = SemVer.parse(installedVersionName.versionName())
            ?: return UpdateCheckResult.NoUsableRelease

        if (remoteVersion <= installedVersion) {
            attemptStore.clearPendingUpdate()
            return UpdateCheckResult.UpToDate
        }

        val notes = release.body?.trim()?.takeIf { it.isNotEmpty() }
        val url = release.htmlUrl.trim()
        if (url.isEmpty() || !isAllowedReleasePageUrl(url)) {
            return UpdateCheckResult.NoUsableRelease
        }

        val update = AppUpdate(
            versionName = remoteVersion.toString(),
            releaseNotes = notes,
            releasePageUrl = url,
        )
        attemptStore.setPendingUpdate(update)
        return UpdateCheckResult.Available(update)
    }

    private suspend fun pendingAvailableOrSkipped(): UpdateCheckResult {
        val pending = attemptStore.getPendingUpdate() ?: return UpdateCheckResult.SkippedWithinInterval
        val pendingVersion = SemVer.parse(pending.versionName)
            ?: run {
                attemptStore.clearPendingUpdate()
                return UpdateCheckResult.SkippedWithinInterval
            }
        val installedVersion = SemVer.parse(installedVersionName.versionName())
            ?: return UpdateCheckResult.SkippedWithinInterval
        if (pendingVersion <= installedVersion || !isAllowedReleasePageUrl(pending.releasePageUrl)) {
            attemptStore.clearPendingUpdate()
            return UpdateCheckResult.SkippedWithinInterval
        }
        return UpdateCheckResult.Available(pending)
    }

    companion object {
        const val DEFAULT_INTERVAL_MILLIS: Long = 24L * 60L * 60L * 1000L
    }
}
