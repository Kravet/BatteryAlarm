package com.example.batteryalarm.analytics

import java.lang.reflect.Proxy
import org.junit.Assert.assertEquals
import org.junit.Test

class AppAnalyticsSafeCallTest {
    @Test
    fun `analytics exception is isolated from caller`() {
        val analytics = Proxy.newProxyInstance(
            AppAnalytics::class.java.classLoader,
            arrayOf(AppAnalytics::class.java),
        ) { _, _, _ -> throw IllegalStateException("analytics unavailable") } as AppAnalytics
        var alarmActionCount = 0

        analytics.recordSafely { logAlarmToggled(enabled = true) }
        alarmActionCount += 1

        assertEquals(1, alarmActionCount)
    }
}
