package com.example.batteryalarm.update

object GitHubReleaseUrl {
    private val HTTPS_GITHUB_URL =
        Regex("^https://(www\\.)?github\\.com/([^/]+)/([^/]+)/.*$", RegexOption.IGNORE_CASE)

    fun isAllowedReleasePageUrl(
        url: String,
        owner: String,
        repo: String,
    ): Boolean {
        val trimmedOwner = owner.trim()
        val trimmedRepo = repo.trim()
        if (trimmedOwner.isEmpty() || trimmedRepo.isEmpty()) {
            return false
        }
        val match = HTTPS_GITHUB_URL.matchEntire(url.trim()) ?: return false
        val urlOwner = match.groupValues[2]
        val urlRepo = match.groupValues[3]
        return urlOwner.equals(trimmedOwner, ignoreCase = true) &&
            urlRepo.equals(trimmedRepo, ignoreCase = true)
    }
}
