package com.example.batteryalarm.domain

/**
 * Controls whether the app keeps listening for system battery-low events.
 *
 * When enabled, the platform should run a minimal foreground service that
 * dynamically subscribes to battery-low broadcasts so events are delivered
 * even when the main application process is not alive. When disabled, monitoring
 * stops and any active subscription is torn down.
 */
interface BatteryLowMonitoring {
    fun setEnabled(enabled: Boolean)
}
