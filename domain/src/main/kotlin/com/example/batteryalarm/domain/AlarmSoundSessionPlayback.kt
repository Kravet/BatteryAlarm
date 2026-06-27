package com.example.batteryalarm.domain

interface AlarmSoundSessionPlayback {
    fun beginLoopingFromSilence()

    fun setVolume(volume: Float)

    fun tearDown()
}
