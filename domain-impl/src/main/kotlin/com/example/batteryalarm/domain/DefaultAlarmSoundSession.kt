package com.example.batteryalarm.domain

internal class DefaultAlarmSoundSession(
    private val volumeRamp: VolumeRampController,
) : AlarmSoundSession {
    private val lock = Any()
    private var activePlayback: AlarmSoundSessionPlayback? = null

    override fun start(playback: AlarmSoundSessionPlayback) {
        synchronized(lock) {
            stopLocked()
            activePlayback = playback
            playback.beginLoopingFromSilence()
            volumeRamp.start { volume ->
                synchronized(lock) {
                    activePlayback?.takeIf { it === playback }?.setVolume(volume)
                }
            }
        }
    }

    override fun stop() {
        synchronized(lock) {
            stopLocked()
        }
    }

    private fun stopLocked() {
        volumeRamp.stop()
        activePlayback?.tearDown()
        activePlayback = null
    }
}
