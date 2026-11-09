package com.example.batteryalarm.analytics

interface AppErrorReporter {
    fun recordNonFatal(stage: String, exception: Exception)
}

fun AppErrorReporter.recordSafely(stage: String, exception: Exception) {
    try {
        recordNonFatal(stage, exception)
    } catch (_: Exception) {
        // Crash reporting must not affect the alarm operation.
    }
}
