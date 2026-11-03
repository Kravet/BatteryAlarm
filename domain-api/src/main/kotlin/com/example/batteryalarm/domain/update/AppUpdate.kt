package com.example.batteryalarm.domain.update

data class AppUpdate(
    val versionName: String,
    val releaseNotes: String?,
    val releasePageUrl: String,
)

data class LatestStableRelease(
    val tagName: String,
    val body: String?,
    val htmlUrl: String,
)

sealed interface UpdateCheckResult {
    data object Disabled : UpdateCheckResult
    data object SkippedWithinInterval : UpdateCheckResult
    data object UpToDate : UpdateCheckResult
    data object NoUsableRelease : UpdateCheckResult
    data class Available(val update: AppUpdate) : UpdateCheckResult
}
