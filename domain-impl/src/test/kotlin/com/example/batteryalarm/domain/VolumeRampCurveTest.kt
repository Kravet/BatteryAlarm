package com.example.batteryalarm.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VolumeRampCurveTest {
    @Test
    fun `when levels are generated, then first is near zero and last is full volume`() {
        val config = VolumeRampConfig(durationMs = 3_000L, stepMs = 300L)
        val levels = VolumeRampCurve.levels(config)

        assertEquals(10, levels.size)
        assertTrue(levels.first() < 0.05f)
        assertEquals(1f, levels.last(), 0.001f)
    }

    @Test
    fun `when levels are generated, then they increase monotonically`() {
        val levels = VolumeRampCurve.levels(
            VolumeRampConfig(durationMs = 3_000L, stepMs = 300L),
        )

        levels.zipWithNext().forEach { (previous, next) ->
            assertTrue(previous < next)
        }
    }

    @Test
    fun `when ease exponent is quadratic, then midpoint is below linear`() {
        val steps = 10
        val easedMidpoint = VolumeRampCurve.volumeAtStep(
            step = steps / 2,
            steps = steps,
            easeExponent = 2f,
        )
        val linearMidpoint = 0.5f

        assertTrue(easedMidpoint < linearMidpoint)
    }

    @Test
    fun `when default config is used, then step count matches alarm ramp duration`() {
        val config = VolumeRampConfig()

        assertEquals(100, config.stepCount)
        assertEquals(100, VolumeRampCurve.levels(config).size)
    }
}
