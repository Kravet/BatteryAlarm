package com.example.batteryalarm.domain

import kotlin.math.pow

object VolumeRampCurve {
    fun volumeAtStep(step: Int, steps: Int, easeExponent: Float): Float {
        require(step in 1..steps) { "step must be in 1..steps" }
        require(steps > 0) { "steps must be positive" }
        require(easeExponent > 0f) { "easeExponent must be positive" }

        val progress = step.toFloat() / steps
        return progress.pow(easeExponent).coerceIn(0f, 1f)
    }

    fun levels(config: VolumeRampConfig): List<Float> =
        (1..config.stepCount).map { step ->
            volumeAtStep(
                step = step,
                steps = config.stepCount,
                easeExponent = config.easeExponent,
            )
        }
}
