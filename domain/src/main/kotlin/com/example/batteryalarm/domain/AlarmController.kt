package com.example.batteryalarm.domain

interface AlarmController {
    val state: AlarmState

    fun startAlarm(reason: AlarmStartReason)

    fun stopAlarm(reason: AlarmStopReason)
}
