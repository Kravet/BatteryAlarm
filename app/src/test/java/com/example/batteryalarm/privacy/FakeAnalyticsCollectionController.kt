package com.example.batteryalarm.privacy

class FakeAnalyticsCollectionController : AnalyticsCollectionController {
    val enabledHistory = mutableListOf<Boolean>()

    override fun setCollectionEnabled(enabled: Boolean) {
        enabledHistory += enabled
    }
}
