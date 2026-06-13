package com.example.batteryalarm.settings

import android.content.Context
import com.example.batteryalarm.domain.AlarmSettingsRepository

class AndroidAlarmSettingsRepository(
    private val context: Context,
) : AlarmSettingsRepository {
    override fun isAlarmEnabled(): Boolean = true
}
