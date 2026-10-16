package com.example.batteryalarm

import android.app.Application
import android.util.Log
import com.example.batteryalarm.di.ApplicationScope
import com.example.batteryalarm.domain.AlarmSettingsCoordinator
import com.example.batteryalarm.domain.privacy.PrivacyConsentRepository
import com.example.batteryalarm.privacy.FirebaseConsentApplier
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class BatteryAlarmApp : Application() {
    @Inject
    lateinit var alarmSettingsCoordinator: AlarmSettingsCoordinator

    @Inject
    lateinit var privacyConsentRepository: PrivacyConsentRepository

    @Inject
    lateinit var firebaseConsentApplier: FirebaseConsentApplier

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Application onCreate")
        Log.d(TAG, "Starting alarm settings coordinator")
        alarmSettingsCoordinator.start()
        applicationScope.launch {
            privacyConsentRepository.consent
                .catch { exception ->
                    Log.w(TAG, "Privacy consent flow failed: ${exception.message}")
                }
                .collect { consent ->
                    firebaseConsentApplier.apply(
                        analyticsEnabled = consent.effectiveAnalyticsEnabled,
                        crashlyticsEnabled = consent.effectiveCrashlyticsEnabled,
                    )
                }
        }
    }

    private companion object {
        const val TAG = "BatteryAlarmApp"
    }
}
