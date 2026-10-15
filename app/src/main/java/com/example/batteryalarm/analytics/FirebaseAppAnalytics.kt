package com.example.batteryalarm.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAppAnalytics @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics,
) : AppAnalytics {
    @Volatile
    private var alarmStartedAtEpochMs: Long? = null

    override fun logAlarmToggled(enabled: Boolean) {
        log(EVENT_ALARM_TOGGLED) {
            putBoolean(PARAM_ENABLED, enabled)
        }
    }

    override fun logNotificationPermissionDenied() {
        log(EVENT_NOTIFICATION_PERMISSION_DENIED)
    }

    override fun logTestAlarmStarted() {
        log(EVENT_TEST_ALARM_STARTED)
    }

    override fun logTestAlarmFailed() {
        log(EVENT_TEST_ALARM_FAILED)
    }

    override fun logAlarmStarted(reason: String, hourOfDay: Int) {
        alarmStartedAtEpochMs = System.currentTimeMillis()
        log(EVENT_ALARM_STARTED) {
            putString(PARAM_REASON, reason)
            putLong(PARAM_HOUR_OF_DAY, hourOfDay.toLong())
        }
    }

    override fun logAlarmStartBlocked(result: String) {
        log(EVENT_ALARM_START_BLOCKED) {
            putString(PARAM_RESULT, result)
        }
    }

    override fun logAlarmStopped(reason: String, durationSec: Long?) {
        val startedAt = alarmStartedAtEpochMs
        alarmStartedAtEpochMs = null
        val resolvedDurationSec = durationSec
            ?: startedAt?.let { ((System.currentTimeMillis() - it) / 1000L).coerceAtLeast(0L) }
        log(EVENT_ALARM_STOPPED) {
            putString(PARAM_REASON, reason)
            if (resolvedDurationSec != null) {
                putLong(PARAM_DURATION_SEC, resolvedDurationSec)
            }
        }
    }

    override fun logUpdateAvailableShown(version: String) {
        log(EVENT_UPDATE_AVAILABLE_SHOWN) {
            putString(PARAM_VERSION, version)
        }
    }

    override fun logUpdateClicked(version: String) {
        log(EVENT_UPDATE_CLICKED) {
            putString(PARAM_VERSION, version)
        }
    }

    private inline fun log(name: String, buildParams: Bundle.() -> Unit = {}) {
        val params = Bundle().apply(buildParams)
        firebaseAnalytics.logEvent(name, params.takeUnless { it.isEmpty })
    }

    private companion object {
        const val EVENT_ALARM_TOGGLED = "alarm_toggled"
        const val EVENT_NOTIFICATION_PERMISSION_DENIED = "notification_permission_denied"
        const val EVENT_TEST_ALARM_STARTED = "test_alarm_started"
        const val EVENT_TEST_ALARM_FAILED = "test_alarm_failed"
        const val EVENT_ALARM_STARTED = "alarm_started"
        const val EVENT_ALARM_START_BLOCKED = "alarm_start_blocked"
        const val EVENT_ALARM_STOPPED = "alarm_stopped"
        const val EVENT_UPDATE_AVAILABLE_SHOWN = "update_available_shown"
        const val EVENT_UPDATE_CLICKED = "update_clicked"

        const val PARAM_ENABLED = "enabled"
        const val PARAM_REASON = "reason"
        const val PARAM_RESULT = "result"
        const val PARAM_HOUR_OF_DAY = "hour_of_day"
        const val PARAM_DURATION_SEC = "duration_sec"
        const val PARAM_VERSION = "version"
    }
}
