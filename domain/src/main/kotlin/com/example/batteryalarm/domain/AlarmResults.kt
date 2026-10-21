package com.example.batteryalarm.domain

sealed interface AlarmStartResult {
    data class Started(
        val state: AlarmState.Active,
    ) : AlarmStartResult

    data class AlreadyActive(
        val state: AlarmState.Active,
    ) : AlarmStartResult

    data object Disabled : AlarmStartResult

    /** A stop was requested while startAlarm was waiting for its settings check. */
    data object Superseded : AlarmStartResult
}

sealed interface AlarmStopResult {
    data class Stopped(
        val previousState: AlarmState.Active,
        val reason: AlarmStopReason,
    ) : AlarmStopResult

    data object AlreadyIdle : AlarmStopResult
}
