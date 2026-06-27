package com.example.batteryalarm.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.example.batteryalarm.domain.AlarmSettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.alarmSettingsDataStore by preferencesDataStore(name = "alarm_settings")

class AndroidAlarmSettingsRepository(
    context: Context,
) : AlarmSettingsRepository {
    private val appContext = context.applicationContext
    private val dataStore = appContext.alarmSettingsDataStore

    override val alarmEnabled: Flow<Boolean> = dataStore.data
        .map { preferences -> preferences[KEY_ALARM_ENABLED] ?: false }
        .distinctUntilChanged()

    override suspend fun isAlarmEnabled(): Boolean {
        return alarmEnabled.first()
    }

    override suspend fun setAlarmEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_ALARM_ENABLED] = enabled
        }
    }

    override suspend fun toggleAlarmEnabled() {
        dataStore.edit { preferences ->
            val current = preferences[KEY_ALARM_ENABLED] ?: false
            preferences[KEY_ALARM_ENABLED] = !current
        }
    }

    private companion object {
        val KEY_ALARM_ENABLED = booleanPreferencesKey("alarm_enabled")
    }
}
