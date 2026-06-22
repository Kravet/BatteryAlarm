package com.example.batteryalarm.alarm

import org.junit.Assert.assertEquals
import org.junit.Test

class BatteryLowMonitoringServiceIntentsTest {
    @Test
    fun `start action is the stable expected value`() {
        assertEquals(
            "com.example.batteryalarm.action.START_BATTERY_LOW_MONITORING",
            BatteryLowMonitoringService.ACTION_START,
        )
    }
}
