package com.example.batteryalarm.domain

interface AlarmSettingsRepository {
    fun isAlarmEnabled(): Boolean

    fun setAlarmEnabled(enabled: Boolean)
}
