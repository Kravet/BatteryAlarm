package com.example.batteryalarm.privacy

import android.util.Log
import com.example.batteryalarm.di.ApplicationScope
import com.example.batteryalarm.domain.privacy.PrivacyConsentRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * The application-scoped owner of Firebase consent side effects.
 * ViewModels only persist consent; this coordinator observes the persisted source of truth.
 */
@Singleton
class FirebaseConsentCoordinator @Inject constructor(
    private val repository: PrivacyConsentRepository,
    private val applier: FirebaseConsentApplier,
    @ApplicationScope private val scope: CoroutineScope,
) {
    private var started = false

    @Synchronized
    fun start() {
        if (started) return
        started = true

        scope.launch {
            repository.consent
                .map { consent ->
                    consent.effectiveAnalyticsEnabled to consent.effectiveCrashlyticsEnabled
                }
                .distinctUntilChanged()
                .catch { exception ->
                    if (exception is CancellationException) throw exception
                    Log.w(TAG, "Privacy consent observation failed", exception)
                }
                .collect { (analyticsEnabled, crashlyticsEnabled) ->
                    applier.apply(
                        analyticsEnabled = analyticsEnabled,
                        crashlyticsEnabled = crashlyticsEnabled,
                    )
                }
        }
    }

    private companion object {
        const val TAG = "FirebaseConsent"
    }
}
