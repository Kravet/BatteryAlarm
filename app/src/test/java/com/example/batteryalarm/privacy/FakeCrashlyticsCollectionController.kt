package com.example.batteryalarm.privacy

class FakeCrashlyticsCollectionController : CrashlyticsCollectionController {
    val enabledHistory = mutableListOf<Boolean>()
    var deleteUnsentReportsCount = 0

    override fun setCollectionEnabled(enabled: Boolean) {
        enabledHistory += enabled
    }

    override fun deleteUnsentReports() {
        deleteUnsentReportsCount += 1
    }
}
