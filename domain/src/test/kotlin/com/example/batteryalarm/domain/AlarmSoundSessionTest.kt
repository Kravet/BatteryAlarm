package com.example.batteryalarm.domain

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AlarmSoundSessionTest {
    @Test
    fun `when session starts, then playback begins silent and receives ramped volumes`() = runTest {
        val playback = RecordingPlayback()
        val session = AlarmSoundSession(
            volumeRamp = VolumeRampController(
                scope = this,
                config = VolumeRampConfig(durationMs = 900L, stepMs = 300L),
                delayStep = {},
            ),
        )

        session.start(playback)
        runCurrent()

        assertTrue(playback.beginCount == 1)
        assertEquals(listOf(0f), playback.silentVolumes)
        assertEquals(3, playback.appliedVolumes.size)
        assertEquals(1f, playback.appliedVolumes.last(), 0.001f)
    }

    @Test
    fun `when session stops, then playback is torn down and ramp stops`() = runTest {
        val playback = RecordingPlayback()
        val session = AlarmSoundSession(
            volumeRamp = VolumeRampController(
                scope = this,
                config = VolumeRampConfig(durationMs = 900L, stepMs = 300L),
                delayStep = {},
            ),
        )

        session.start(playback)
        runCurrent()
        session.stop()

        assertEquals(1, playback.tearDownCount)
    }

    @Test
    fun `when session starts again, then volumes apply only to the active playback`() = runTest {
        val firstPlayback = RecordingPlayback()
        val secondPlayback = RecordingPlayback()
        val session = AlarmSoundSession(
            volumeRamp = VolumeRampController(
                scope = this,
                config = VolumeRampConfig(durationMs = 900L, stepMs = 300L),
                delayStep = {},
            ),
        )

        session.start(firstPlayback)
        runCurrent()
        val firstPlaybackVolumeCount = firstPlayback.appliedVolumes.size
        session.start(secondPlayback)
        runCurrent()

        assertEquals(1, firstPlayback.tearDownCount)
        assertEquals(firstPlaybackVolumeCount, firstPlayback.appliedVolumes.size)
        assertEquals(3, secondPlayback.appliedVolumes.size)
    }
}

private class RecordingPlayback : AlarmSoundSessionPlayback {
    var beginCount = 0
        private set
    var tearDownCount = 0
        private set
    val silentVolumes = mutableListOf<Float>()
    val appliedVolumes = mutableListOf<Float>()

    override fun beginLoopingFromSilence() {
        beginCount += 1
        silentVolumes += 0f
    }

    override fun setVolume(volume: Float) {
        appliedVolumes += volume
    }

    override fun tearDown() {
        tearDownCount += 1
    }
}
