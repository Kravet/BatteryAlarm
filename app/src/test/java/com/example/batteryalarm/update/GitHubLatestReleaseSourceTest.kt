package com.example.batteryalarm.update

import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class GitHubLatestReleaseSourceTest {
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `fetches latest stable release from releases latest endpoint`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(
                    """
                    {
                      "tag_name": "v1.2.0",
                      "body": "What's new",
                      "html_url": "https://github.com/example/repo/releases/tag/v1.2.0"
                    }
                    """.trimIndent(),
                ),
        )
        val source = createSource(owner = "Kravet", repo = "battery-alarm-releases")

        val release = source.fetchLatestStableRelease()

        assertEquals("v1.2.0", release?.tagName)
        assertEquals("What's new", release?.body)
        assertEquals("https://github.com/example/repo/releases/tag/v1.2.0", release?.htmlUrl)
        val recorded = server.takeRequest()
        assertEquals("/repos/Kravet/battery-alarm-releases/releases/latest", recorded.path)
        assertEquals("application/vnd.github+json", recorded.getHeader("Accept"))
        assertTrue(recorded.getHeader("User-Agent")!!.isNotBlank())
    }

    @Test
    fun `http error returns null`() = runTest {
        server.enqueue(MockResponse().setResponseCode(404))
        val source = createSource()

        assertNull(source.fetchLatestStableRelease())
    }

    @Test
    fun `malformed json returns null`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody("{not-json"))
        val source = createSource()

        assertNull(source.fetchLatestStableRelease())
    }

    @Test
    fun `missing tag or url returns null`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"tag_name":"","html_url":"https://example.com"}"""),
        )
        val source = createSource()

        assertNull(source.fetchLatestStableRelease())
    }

    private fun createSource(
        owner: String = "owner",
        repo: String = "repo",
    ): GitHubLatestReleaseSource {
        val config = UpdateConfig(
            enabled = true,
            githubOwner = owner,
            githubRepo = repo,
            githubApiBase = server.url("/").toString().trimEnd('/'),
        )
        return GitHubLatestReleaseSource(
            updateConfig = config,
            okHttpClient = OkHttpClient.Builder()
                .connectTimeout(2, TimeUnit.SECONDS)
                .readTimeout(2, TimeUnit.SECONDS)
                .build(),
        )
    }
}
