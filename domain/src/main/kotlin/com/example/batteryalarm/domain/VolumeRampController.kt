package com.example.batteryalarm.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

class VolumeRampController(
    private val scope: CoroutineScope,
    private val config: VolumeRampConfig = VolumeRampConfig(),
    private val delayStep: suspend (Long) -> Unit = { delay(it) },
) {
    private var job: Job? = null

    fun start(applyVolume: (Float) -> Unit) {
        job?.cancel()
        job = scope.launch {
            runRamp(applyVolume)
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    internal suspend fun runRamp(applyVolume: (Float) -> Unit) {
        val steps = config.stepCount
        for (step in 1..steps) {
            delayStep(config.stepMs)
            if (!coroutineContext.isActive) {
                return
            }
            applyVolume(
                VolumeRampCurve.volumeAtStep(
                    step = step,
                    steps = steps,
                    easeExponent = config.easeExponent,
                ),
            )
        }
    }
}
