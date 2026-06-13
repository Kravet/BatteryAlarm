package com.example.batteryalarm.settings

import android.content.Context
import com.example.batteryalarm.domain.AlarmSettingsRepository

class AndroidAlarmSettingsRepository(
    private val context: Context,
) : AlarmSettingsRepository {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    override fun isAlarmEnabled(): Boolean = preferences.getBoolean(KEY_ALARM_ENABLED, false)

    override fun setAlarmEnabled(enabled: Boolean) {
        preferences.edit()
            .putBoolean(KEY_ALARM_ENABLED, enabled)
            .apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "alarm_settings"
        const val KEY_ALARM_ENABLED = "alarm_enabled"
    }
}
