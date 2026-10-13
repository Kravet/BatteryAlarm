package com.example.batteryalarm.update

import com.example.batteryalarm.domain.update.LatestStableRelease
import com.example.batteryalarm.domain.update.LatestStableReleaseSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class GitHubLatestReleaseSource(
    private val updateConfig: UpdateConfig,
    private val okHttpClient: OkHttpClient = defaultClient(),
) : LatestStableReleaseSource {
    override suspend fun fetchLatestStableRelease(): LatestStableRelease? = withContext(Dispatchers.IO) {
        val url =
            "${updateConfig.githubApiBase}/repos/${updateConfig.githubOwner}/${updateConfig.githubRepo}/releases/latest"
        val request = Request.Builder()
            .url(url)
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", USER_AGENT)
            .get()
            .build()

        try {
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext null
                }
                val body = response.body?.string().orEmpty()
                if (body.isBlank()) {
                    return@withContext null
                }
                parseRelease(body)
            }
        } catch (_: IOException) {
            null
        } catch (_: Exception) {
            null
        }
    }

    private fun parseRelease(json: String): LatestStableRelease? {
        val root = JSONObject(json)
        val tagName = root.optString("tag_name").trim()
        val htmlUrl = root.optString("html_url").trim()
        if (tagName.isEmpty() || htmlUrl.isEmpty()) {
            return null
        }
        val body = root.optString("body").trim().takeIf { it.isNotEmpty() }
        return LatestStableRelease(
            tagName = tagName,
            body = body,
            htmlUrl = htmlUrl,
        )
    }

    companion object {
        private const val USER_AGENT = "BatteryAlarm-UpdateChecker"

        fun defaultClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .callTimeout(15, TimeUnit.SECONDS)
            .build()
    }
}
