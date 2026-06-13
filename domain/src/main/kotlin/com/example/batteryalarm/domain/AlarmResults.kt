package com.example.batteryalarm.domain

sealed interface AlarmStartResult {
    data class Started(
        val state: AlarmState.Active,
    ) : AlarmStartResult

    data class AlreadyActive(
        val state: AlarmState.Active,
    ) : AlarmStartResult

    data object Disabled : AlarmStartResult
}

sealed interface AlarmStopResult {
    data class Stopped(
        val previousState: AlarmState.Active,
        val reason: AlarmStopReason,
    ) : AlarmStopResult

    data object AlreadyIdle : AlarmStopResult
}
