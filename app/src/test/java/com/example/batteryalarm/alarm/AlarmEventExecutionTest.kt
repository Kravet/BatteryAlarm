package com.example.batteryalarm.alarm

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.fail
import org.junit.Test

class AlarmEventExecutionTest {
    @Test
    fun `successful operation finishes without reporting a failure`() = runTest {
        var operations = 0
        var finishes = 0
        val failures = mutableListOf<Exception>()

        runAlarmEvent(
            onFailure = { failures += it },
            onFinished = { finishes++ },
        ) {
            operations++
        }

        assertEquals(1, operations)
        assertEquals(1, finishes)
        assertEquals(emptyList<Exception>(), failures)
    }

    @Test
    fun `operational failure is reported and broadcast is finished exactly once`() = runTest {
        val failure = IllegalStateException("audio output failed")
        val failures = mutableListOf<Exception>()
        var finishes = 0

        runAlarmEvent(
            onFailure = { failures += it },
            onFinished = { finishes++ },
        ) {
            throw failure
        }

        assertEquals(listOf(failure), failures)
        assertEquals(1, finishes)
    }

    @Test
    fun `cancellation propagates and still finishes broadcast`() = runTest {
        val cancellation = CancellationException("scope cancelled")
        val failures = mutableListOf<Exception>()
        var finishes = 0

        try {
            runAlarmEvent(
                onFailure = { failures += it },
                onFinished = { finishes++ },
            ) {
                throw cancellation
            }
            fail("Expected cancellation")
        } catch (actual: CancellationException) {
            assertSame(cancellation, actual)
        }

        assertEquals(emptyList<Exception>(), failures)
        assertEquals(1, finishes)
    }
}
