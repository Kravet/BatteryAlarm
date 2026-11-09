package com.example.batteryalarm.analytics

import java.lang.reflect.Proxy
import org.junit.Assert.assertEquals
import org.junit.Test

class AppErrorReporterSafeCallTest {
    @Test
    fun `error reporting exception is isolated from caller`() {
        val reporter = Proxy.newProxyInstance(
            AppErrorReporter::class.java.classLoader,
            arrayOf(AppErrorReporter::class.java),
        ) { _, _, _ -> throw IllegalStateException("crash reporting unavailable") } as AppErrorReporter
        var alarmActionCount = 0

        reporter.recordSafely("alarm_start", IllegalStateException("alarm failed"))
        alarmActionCount += 1

        assertEquals(1, alarmActionCount)
    }
}
