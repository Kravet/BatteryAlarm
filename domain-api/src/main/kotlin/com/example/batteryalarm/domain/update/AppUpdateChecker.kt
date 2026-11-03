package com.example.batteryalarm.domain.update

fun interface AppUpdateChecker {
    suspend operator fun invoke(): UpdateCheckResult
}

fun interface ReleasePageUrlPolicy {
    fun isAllowed(url: String): Boolean
}
