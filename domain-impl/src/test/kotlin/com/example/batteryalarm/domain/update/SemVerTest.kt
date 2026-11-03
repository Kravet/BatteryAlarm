package com.example.batteryalarm.domain.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SemVerTest {
    @Test
    fun `parses MAJOR MINOR PATCH`() {
        assertEquals(SemVer(1, 2, 3), SemVer.parse("1.2.3"))
    }

    @Test
    fun `parses tag with leading v`() {
        assertEquals(SemVer(1, 2, 3), SemVer.parse("v1.2.3"))
    }

    @Test
    fun `compares patch versions numerically not lexicographically`() {
        assertTrue(SemVer.parse("1.9.0")!! < SemVer.parse("1.10.0")!!)
        assertTrue(SemVer.parse("1.10.0")!! < SemVer.parse("2.0.0")!!)
    }

    @Test
    fun `equal versions compare equal`() {
        assertEquals(0, SemVer.parse("v1.0.0")!!.compareTo(SemVer.parse("1.0.0")!!))
    }

    @Test
    fun `rejects prerelease suffix`() {
        assertNull(SemVer.parse("v1.0.0-beta.1"))
        assertNull(SemVer.parse("1.0.0-rc.1"))
    }

    @Test
    fun `rejects build metadata suffix`() {
        assertNull(SemVer.parse("1.0.0+meta"))
    }

    @Test
    fun `rejects incomplete or malformed tags`() {
        assertNull(SemVer.parse("1.0"))
        assertNull(SemVer.parse("abc"))
        assertNull(SemVer.parse(""))
        assertNull(SemVer.parse("   "))
        assertNull(SemVer.parse("v1.0.0.1"))
    }
}
