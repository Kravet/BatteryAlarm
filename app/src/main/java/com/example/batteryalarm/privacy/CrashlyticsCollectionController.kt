package com.example.batteryalarm.privacy

interface CrashlyticsCollectionController {
    fun setCollectionEnabled(enabled: Boolean)

    fun deleteUnsentReports()
}
