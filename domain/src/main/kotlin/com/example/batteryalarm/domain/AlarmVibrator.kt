package com.example.batteryalarm.domain

interface AlarmVibrator {
    fun startLooping()

    fun stop()
}
