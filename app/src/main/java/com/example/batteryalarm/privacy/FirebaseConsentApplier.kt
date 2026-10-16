package com.example.batteryalarm.privacy

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseConsentApplier @Inject constructor(
    private val analyticsCollection: AnalyticsCollectionController,
    private val crashlyticsCollection: CrashlyticsCollectionController,
) {
    fun apply(analyticsEnabled: Boolean, crashlyticsEnabled: Boolean) {
        analyticsCollection.setCollectionEnabled(analyticsEnabled)
        crashlyticsCollection.setCollectionEnabled(crashlyticsEnabled)
        if (!crashlyticsEnabled) {
            crashlyticsCollection.deleteUnsentReports()
        }
    }
}
