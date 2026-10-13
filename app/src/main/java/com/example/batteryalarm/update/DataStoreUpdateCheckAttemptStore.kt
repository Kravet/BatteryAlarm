package com.example.batteryalarm.update

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.batteryalarm.domain.update.AppUpdate
import com.example.batteryalarm.domain.update.UpdateCheckAttemptStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.updateCheckDataStore by preferencesDataStore(name = "update_check")

class DataStoreUpdateCheckAttemptStore(
    appContext: Context,
) : UpdateCheckAttemptStore {
    private val dataStore = appContext.updateCheckDataStore

    override suspend fun getLastCheckAttemptAtMillis(): Long? {
        return dataStore.data
            .map { preferences -> preferences[KEY_LAST_CHECK_ATTEMPT_AT] }
            .first()
    }

    override suspend fun setLastCheckAttemptAtMillis(epochMillis: Long) {
        dataStore.edit { preferences ->
            preferences[KEY_LAST_CHECK_ATTEMPT_AT] = epochMillis
        }
    }

    override suspend fun getPendingUpdate(): AppUpdate? {
        val preferences = dataStore.data.first()
        val versionName = preferences[KEY_PENDING_VERSION] ?: return null
        val releasePageUrl = preferences[KEY_PENDING_URL] ?: return null
        val releaseNotes = preferences[KEY_PENDING_NOTES]
        return AppUpdate(
            versionName = versionName,
            releaseNotes = releaseNotes,
            releasePageUrl = releasePageUrl,
        )
    }

    override suspend fun setPendingUpdate(update: AppUpdate) {
        val notes = update.releaseNotes
        dataStore.edit { preferences ->
            preferences[KEY_PENDING_VERSION] = update.versionName
            preferences[KEY_PENDING_URL] = update.releasePageUrl
            if (notes == null) {
                preferences.remove(KEY_PENDING_NOTES)
            } else {
                preferences[KEY_PENDING_NOTES] = notes
            }
        }
    }

    override suspend fun clearPendingUpdate() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_PENDING_VERSION)
            preferences.remove(KEY_PENDING_URL)
            preferences.remove(KEY_PENDING_NOTES)
        }
    }

    private companion object {
        val KEY_LAST_CHECK_ATTEMPT_AT = longPreferencesKey("last_check_attempt_at")
        val KEY_PENDING_VERSION = stringPreferencesKey("pending_version")
        val KEY_PENDING_URL = stringPreferencesKey("pending_url")
        val KEY_PENDING_NOTES = stringPreferencesKey("pending_notes")
    }
}
