package com.example.batteryalarm.domain

interface AlarmController {
    val state: AlarmState

    fun startAlarm(reason: AlarmStartReason): AlarmStartResult

    fun stopAlarm(reason: AlarmStopReason): AlarmStopResult
}
