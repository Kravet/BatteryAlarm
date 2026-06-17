package com.example.batteryalarm.alarm

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidBatteryLowReceiverRegistrar @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val batteryLowAlarmHandler: BatteryLowAlarmHandler,
) : BatteryLowReceiverRegistrar {
    private val packageManager = appContext.packageManager
    private val batteryLowReceiverComponent = ComponentName(
        appContext,
        BatteryLowReceiver::class.java,
    )
    private var runtimeReceiver: BroadcastReceiver? = null

    override fun setBatteryLowReceiverEnabled(enabled: Boolean) {
        setManifestReceiverEnabled(enabled)
        if (enabled) {
            registerRuntimeReceiver()
        } else {
            unregisterRuntimeReceiver()
        }
    }

    private fun setManifestReceiverEnabled(enabled: Boolean) {
        val componentState = if (enabled) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        }

        packageManager.setComponentEnabledSetting(
            batteryLowReceiverComponent,
            componentState,
            PackageManager.DONT_KILL_APP,
        )
    }

    private fun registerRuntimeReceiver() {
        if (runtimeReceiver != null) {
            return
        }

        runtimeReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action != Intent.ACTION_BATTERY_LOW) {
                    return
                }

                val pendingResult = goAsync()
                batteryLowAlarmHandler.handleBatteryLow(context) {
                    pendingResult.finish()
                }
            }
        }

        appContext.registerReceiver(
            runtimeReceiver,
            IntentFilter(Intent.ACTION_BATTERY_LOW),
            Context.RECEIVER_NOT_EXPORTED,
        )
    }

    private fun unregisterRuntimeReceiver() {
        val receiver = runtimeReceiver ?: return
        appContext.unregisterReceiver(receiver)
        runtimeReceiver = null
    }
}
