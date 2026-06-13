package com.example.batteryalarm.domain

interface AlarmSettingsRepository {
    suspend fun isAlarmEnabled(): Boolean

    suspend fun setAlarmEnabled(enabled: Boolean): Boolean
}
