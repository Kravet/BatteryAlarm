package com.example.batteryalarm.privacy

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.batteryalarm.domain.privacy.PrivacyConsent
import com.example.batteryalarm.domain.privacy.PrivacyConsentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private val Context.privacyConsentDataStore by preferencesDataStore(name = "privacy_consent")

class DataStorePrivacyConsentRepository(
    context: Context,
) : PrivacyConsentRepository {
    private val dataStore = context.applicationContext.privacyConsentDataStore

    override val consent: Flow<PrivacyConsent> = dataStore.data
        .map { preferences ->
            PrivacyConsent(
                analyticsEnabled = preferences[KEY_ANALYTICS_ENABLED] ?: false,
                crashlyticsEnabled = preferences[KEY_CRASHLYTICS_ENABLED] ?: false,
                decisionRecorded = preferences[KEY_DECISION_RECORDED] ?: false,
                decidedAtEpochMs = preferences[KEY_DECIDED_AT_EPOCH_MS],
                consentTextVersion = preferences[KEY_CONSENT_TEXT_VERSION],
            )
        }
        .distinctUntilChanged()

    override suspend fun setConsent(
        analyticsEnabled: Boolean,
        crashlyticsEnabled: Boolean,
        decidedAtEpochMs: Long,
        consentTextVersion: String,
    ) {
        dataStore.edit { preferences ->
            preferences[KEY_ANALYTICS_ENABLED] = analyticsEnabled
            preferences[KEY_CRASHLYTICS_ENABLED] = crashlyticsEnabled
            preferences[KEY_DECISION_RECORDED] = true
            preferences[KEY_DECIDED_AT_EPOCH_MS] = decidedAtEpochMs
            preferences[KEY_CONSENT_TEXT_VERSION] = consentTextVersion
        }
    }

    /** Clears stored consent so the onboarding prompt can be shown again (used by tests). */
    suspend fun clearConsent() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    private companion object {
        val KEY_ANALYTICS_ENABLED = booleanPreferencesKey("analytics_enabled")
        val KEY_CRASHLYTICS_ENABLED = booleanPreferencesKey("crashlytics_enabled")
        val KEY_DECISION_RECORDED = booleanPreferencesKey("decision_recorded")
        val KEY_DECIDED_AT_EPOCH_MS = longPreferencesKey("decided_at_epoch_ms")
        val KEY_CONSENT_TEXT_VERSION = stringPreferencesKey("consent_text_version")
    }
}
