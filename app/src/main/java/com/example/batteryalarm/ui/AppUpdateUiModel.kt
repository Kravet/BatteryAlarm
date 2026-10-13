package com.example.batteryalarm.ui

import com.example.batteryalarm.domain.update.AppUpdate

data class AppUpdateUiModel(
    val versionName: String,
    val releaseNotes: String?,
    val releasePageUrl: String,
) {
    companion object {
        fun from(update: AppUpdate): AppUpdateUiModel = AppUpdateUiModel(
            versionName = update.versionName,
            releaseNotes = update.releaseNotes,
            releasePageUrl = update.releasePageUrl,
        )
    }
}
