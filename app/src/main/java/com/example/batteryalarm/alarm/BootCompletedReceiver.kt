package com.example.batteryalarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.batteryalarm.domain.AlarmSettingsCoordinator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Wakes the process after reboot so the [AlarmSettingsCoordinator] can run.
 *
 * The coordinator is the single owner of "settings -> monitoring": once started it
 * observes the persisted alarm setting and (re)starts the foreground monitoring
 * service when enabled. This receiver only guarantees the coordinator is started
 * after boot; it intentionally does not restore monitoring itself to avoid
 * duplicating that logic.
 */
@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {
    @Inject
    lateinit var alarmSettingsCoordinator: AlarmSettingsCoordinator

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }

        Log.d(TAG, "BOOT_COMPLETED received")
        Log.d(TAG, "Ensuring alarm settings coordinator is started after boot")
        alarmSettingsCoordinator.start()
    }

    private companion object {
        const val TAG = "BootCompletedReceiver"
    }
}
