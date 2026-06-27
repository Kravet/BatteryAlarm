package com.example.batteryalarm.domain

import kotlinx.coroutines.flow.Flow

interface AlarmSettingsRepository {
    val alarmEnabled: Flow<Boolean>

    suspend fun isAlarmEnabled(): Boolean

    suspend fun setAlarmEnabled(enabled: Boolean)

    suspend fun toggleAlarmEnabled()
}
