package com.example.batteryalarm.privacy

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.URI

class PrivacyPolicyLinksTest {
    @Test
    fun `public url points at battery-alarm-releases PRIVACY md over https`() {
        val uri = URI(PrivacyPolicyLinks.PUBLIC_URL)

        assertEquals("https", uri.scheme)
        assertEquals("raw.githubusercontent.com", uri.host)
        assertEquals(
            "/Kravet/battery-alarm-releases/main/PRIVACY.md",
            uri.path,
        )
    }

    @Test
    fun `public url is a raw markdown document path`() {
        assertTrue(PrivacyPolicyLinks.PUBLIC_URL.endsWith("/PRIVACY.md"))
        assertTrue(PrivacyPolicyLinks.PUBLIC_URL.startsWith("https://"))
    }
}
