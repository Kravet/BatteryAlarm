package com.example.batteryalarm.domain

class DefaultAlarmController(
    private val settingsRepository: AlarmSettingsRepository,
    private val soundPlayer: AlarmSoundPlayer,
    private val vibrator: AlarmVibrator,
    private val notifier: AlarmNotifier,
) : AlarmController {
    // Output operations and state changes form one critical section.
    // No suspending call may execute while this monitor is held.
    private val lock = Any()
    private var stopGeneration: Long = 0L
    private var currentState: AlarmState = AlarmState.Idle

    override val state: AlarmState
        get() = synchronized(lock) { currentState }

    override suspend fun startAlarm(reason: AlarmStartReason): AlarmStartResult {
        // A stop that happens during the suspend check invalidates this older start request.
        val startingGeneration = synchronized(lock) { stopGeneration }
        // This is the only suspension point; never hold the monitor across it.
        if (reason != AlarmStartReason.TestAlarmFlow && !settingsRepository.isAlarmEnabled()) {
            return AlarmStartResult.Disabled
        }

        return synchronized(lock) {
            if (startingGeneration != stopGeneration) {
                return@synchronized AlarmStartResult.Superseded
            }
            val activeState = currentState as? AlarmState.Active
            if (activeState != null) {
                return@synchronized AlarmStartResult.AlreadyActive(activeState)
            }

            val newState = AlarmState.Active(startReason = reason)
            try {
                soundPlayer.startLooping()
                vibrator.startLooping()
                notifier.showAlarmStarted(reason)
                currentState = newState
                AlarmStartResult.Started(newState)
            } catch (exception: Exception) {
                // Partial startup must not leave a sound, vibration or notification behind.
                runCatching { soundPlayer.stop() }
                runCatching { vibrator.stop() }
                runCatching { notifier.clearAlarm() }
                throw exception
            }
        }
    }

    override fun stopAlarm(reason: AlarmStopReason): AlarmStopResult = synchronized(lock) {
        // Invalidate pending start requests even if the alarm has not started yet.
        stopGeneration++
        val activeState = currentState as? AlarmState.Active
            ?: return@synchronized AlarmStopResult.AlreadyIdle

        // A failing output must not prevent cleanup of the other outputs or state.
        try {
            soundPlayer.stop()
        } finally {
            try {
                vibrator.stop()
            } finally {
                try {
                    notifier.clearAlarm()
                } finally {
                    currentState = AlarmState.Idle
                }
            }
        }

        AlarmStopResult.Stopped(previousState = activeState, reason = reason)
    }
}
