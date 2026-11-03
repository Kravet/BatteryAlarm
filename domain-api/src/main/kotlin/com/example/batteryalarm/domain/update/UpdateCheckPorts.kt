package com.example.batteryalarm.domain.update

interface UpdateCheckAttemptStore {
    suspend fun getLastCheckAttemptAtMillis(): Long?

    suspend fun setLastCheckAttemptAtMillis(epochMillis: Long)

    suspend fun getPendingUpdate(): AppUpdate?

    suspend fun setPendingUpdate(update: AppUpdate)

    suspend fun clearPendingUpdate()
}

interface LatestStableReleaseSource {
    /**
     * Fetches the latest stable GitHub release, or null on network/HTTP/parse failure.
     */
    suspend fun fetchLatestStableRelease(): LatestStableRelease?
}

fun interface InstalledAppVersionName {
    fun versionName(): String
}

fun interface UpdateCheckerEnabled {
    fun isEnabled(): Boolean
}
