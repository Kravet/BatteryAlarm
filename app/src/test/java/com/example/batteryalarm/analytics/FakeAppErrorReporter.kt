package com.example.batteryalarm.analytics

class FakeAppErrorReporter : AppErrorReporter {
    val reported = mutableListOf<Pair<String, Exception>>()

    override fun recordNonFatal(stage: String, exception: Exception) {
        reported += stage to exception
    }
}
