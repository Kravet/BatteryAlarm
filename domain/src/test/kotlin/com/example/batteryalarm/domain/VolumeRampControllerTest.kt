package com.example.batteryalarm.domain

import kotlin.coroutines.resume
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VolumeRampControllerTest {
    @Test
    fun `when ramp runs without delay, then all volume levels are applied`() = runTest {
        val appliedVolumes = mutableListOf<Float>()
        val ramp = VolumeRampController(
            scope = backgroundScope,
            config = VolumeRampConfig(durationMs = 900L, stepMs = 300L),
            delayStep = {},
        )

        ramp.runRamp { volume -> appliedVolumes += volume }

        assertEquals(3, appliedVolumes.size)
        assertEquals(1f, appliedVolumes.last(), 0.001f)
    }

    @Test
    fun `when start is called, then ramp applies all volume levels`() = runTest {
        val appliedVolumes = mutableListOf<Float>()
        val ramp = VolumeRampController(
            scope = this,
            config = VolumeRampConfig(durationMs = 900L, stepMs = 300L),
            delayStep = {},
        )

        ramp.start { volume -> appliedVolumes += volume }
        runCurrent()

        assertEquals(3, appliedVolumes.size)
        assertEquals(1f, appliedVolumes.last(), 0.001f)
    }

    @Test
    fun `when stop is called, then pending ramp steps are cancelled`() = runTest {
        val gate = StepDelayGate()
        val appliedVolumes = mutableListOf<Float>()
        val ramp = VolumeRampController(
            scope = this,
            config = VolumeRampConfig(durationMs = 3_000L, stepMs = 300L),
            delayStep = { gate.awaitStep() },
        )

        ramp.start { volume -> appliedVolumes += volume }
        runCurrent()
        gate.releaseNext()
        runCurrent()
        assertEquals(1, appliedVolumes.size)
        ramp.stop()
        gate.releaseNext()
        gate.releaseNext()
        runCurrent()

        assertEquals(1, appliedVolumes.size)
    }

    @Test
    fun `when start is called again, then previous ramp is replaced`() = runTest {
        val gate = StepDelayGate()
        val appliedVolumes = mutableListOf<Float>()
        val ramp = VolumeRampController(
            scope = this,
            config = VolumeRampConfig(durationMs = 900L, stepMs = 300L),
            delayStep = { gate.awaitStep() },
        )

        ramp.start { volume -> appliedVolumes += volume }
        runCurrent()
        ramp.start { volume -> appliedVolumes += volume }
        runCurrent()
        repeat(3) {
            gate.releaseNext()
            runCurrent()
        }

        assertEquals(3, appliedVolumes.size)
        assertEquals(1f, appliedVolumes.last(), 0.001f)
    }
}

private class StepDelayGate {
    private val awaiters = ArrayDeque<CancellableContinuation<Unit>>()

    suspend fun awaitStep() {
        suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation {
                awaiters.remove(continuation)
            }
            awaiters.addLast(continuation)
        }
    }

    fun releaseNext() {
        val continuation = awaiters.removeFirstOrNull() ?: return
        if (continuation.isCancelled) {
            releaseNext()
            return
        }
        continuation.resume(Unit)
    }
}
