package com.example.batteryalarm.alarm

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

class AndroidBatteryLowReceiverRegistrar(
    context: Context,
) : BatteryLowReceiverRegistrar {
    private val appContext = context.applicationContext
    private val packageManager = appContext.packageManager
    private val batteryLowReceiverComponent = ComponentName(
        appContext,
        BatteryLowReceiver::class.java,
    )

    override fun setBatteryLowReceiverEnabled(enabled: Boolean) {
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
}
