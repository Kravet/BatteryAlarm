package com.example.batteryalarm.domain

class AlarmSoundSession(
    private val volumeRamp: VolumeRampController,
) {
    private val lock = Any()
    private var activePlayback: AlarmSoundSessionPlayback? = null

    fun start(playback: AlarmSoundSessionPlayback) {
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

    fun stop() {
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
