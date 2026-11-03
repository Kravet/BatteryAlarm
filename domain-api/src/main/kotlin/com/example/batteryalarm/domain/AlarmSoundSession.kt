package com.example.batteryalarm.domain

interface AlarmSoundSession {
    fun start(playback: AlarmSoundSessionPlayback)

    fun stop()
}
