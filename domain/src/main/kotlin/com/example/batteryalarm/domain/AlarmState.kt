package com.example.batteryalarm.domain

sealed interface AlarmState {
    data object Idle : AlarmState

    data class Active(
        val startReason: AlarmStartReason,
    ) : AlarmState
}
