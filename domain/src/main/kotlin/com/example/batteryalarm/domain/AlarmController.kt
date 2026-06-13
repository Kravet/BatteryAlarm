package com.example.batteryalarm.domain

interface AlarmController {
    val state: AlarmState

    suspend fun startAlarm(reason: AlarmStartReason): AlarmStartResult

    fun stopAlarm(reason: AlarmStopReason): AlarmStopResult
}
