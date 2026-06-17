package com.example.batteryalarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BatteryLowReceiver : BroadcastReceiver() {
    @Inject
    lateinit var batteryLowAlarmHandler: BatteryLowAlarmHandler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BATTERY_LOW) {
            return
        }

        val pendingResult = goAsync()
        batteryLowAlarmHandler.handleBatteryLow(context) {
            try {
                pendingResult.finish()
            } catch (exception: Exception) {
                Log.w(TAG, "Failed to finish battery low broadcast", exception)
            }
        }
    }

    private companion object {
        const val TAG = "BatteryLowReceiver"
    }
}
