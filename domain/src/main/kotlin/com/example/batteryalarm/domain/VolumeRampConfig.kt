package com.example.batteryalarm.domain

data class VolumeRampConfig(
    val durationMs: Long = DEFAULT_DURATION_MS,
    val stepMs: Long = DEFAULT_STEP_MS,
    val easeExponent: Float = DEFAULT_EASE_EXPONENT,
) {
    val stepCount: Int = (durationMs / stepMs).toInt()

    init {
        require(durationMs > 0L) { "durationMs must be positive" }
        require(stepMs > 0L) { "stepMs must be positive" }
        require(durationMs >= stepMs) { "durationMs must be at least stepMs" }
        require(easeExponent > 0f) { "easeExponent must be positive" }
        require(stepCount > 0) { "stepCount must be positive" }
    }

    companion object {
        const val DEFAULT_DURATION_MS = 30_000L
        const val DEFAULT_STEP_MS = 300L
        const val DEFAULT_EASE_EXPONENT = 2f
    }
}
