package com.example.batteryalarm.analytics

import com.google.firebase.crashlytics.FirebaseCrashlytics

class FirebaseAppErrorReporter(
    private val crashlytics: FirebaseCrashlytics,
) : AppErrorReporter {
    override fun recordNonFatal(stage: String, exception: Exception) {
        try {
            if (!crashlytics.isCrashlyticsCollectionEnabled) return
            crashlytics.log("Alarm diagnostic stage=$stage")
            crashlytics.recordException(exception)
        } catch (_: Exception) {
            // A diagnostic report must not affect the alarm operation.
        }
    }
}
