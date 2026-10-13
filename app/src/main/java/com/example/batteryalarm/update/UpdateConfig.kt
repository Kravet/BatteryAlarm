package com.example.batteryalarm.update

import com.example.batteryalarm.domain.update.UpdateCheckerEnabled

data class UpdateConfig(
    val enabled: Boolean,
    val githubOwner: String,
    val githubRepo: String,
    val githubApiBase: String,
) : UpdateCheckerEnabled {
    override fun isEnabled(): Boolean = enabled
}
