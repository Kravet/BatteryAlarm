package com.example.batteryalarm.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.example.batteryalarm.domain.AlarmSettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.alarmSettingsDataStore by preferencesDataStore(name = "alarm_settings")

class AndroidAlarmSettingsRepository(
    context: Context,
) : AlarmSettingsRepository {
    private val dataStore = context.alarmSettingsDataStore

    override suspend fun isAlarmEnabled(): Boolean {
        return dataStore.data
            .map { preferences -> preferences[KEY_ALARM_ENABLED] ?: false }
            .first()
    }

    override suspend fun setAlarmEnabled(enabled: Boolean): Boolean {
        val savedPreferences = dataStore.edit { preferences ->
            preferences[KEY_ALARM_ENABLED] = enabled
        }
        return savedPreferences[KEY_ALARM_ENABLED] ?: false
    }

    private companion object {
        val KEY_ALARM_ENABLED = booleanPreferencesKey("alarm_enabled")
    }
}
