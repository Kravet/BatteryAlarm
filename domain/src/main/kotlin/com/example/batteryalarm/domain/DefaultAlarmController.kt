package com.example.batteryalarm.domain

class DefaultAlarmController(
    private val settingsRepository: AlarmSettingsRepository,
    private val soundPlayer: AlarmSoundPlayer,
    private val vibrator: AlarmVibrator,
    private val notifier: AlarmNotifier,
) : AlarmController {
    private var currentState: AlarmState = AlarmState.Idle

    override val state: AlarmState
        get() = currentState

    override suspend fun startAlarm(reason: AlarmStartReason): AlarmStartResult {
        if (reason != AlarmStartReason.TestAlarmFlow && !settingsRepository.isAlarmEnabled()) {
            return AlarmStartResult.Disabled
        }

        val activeState = currentState as? AlarmState.Active
        if (activeState != null) {
            return AlarmStartResult.AlreadyActive(activeState)
        }

        val newState = AlarmState.Active(startReason = reason)
        soundPlayer.startLooping()
        vibrator.startLooping()
        notifier.showAlarmStarted(reason)

        currentState = newState
        return AlarmStartResult.Started(newState)
    }

    override fun stopAlarm(reason: AlarmStopReason): AlarmStopResult {
        val activeState = currentState as? AlarmState.Active
        if (activeState == null) {
            return AlarmStopResult.AlreadyIdle
        }

        soundPlayer.stop()
        vibrator.stop()
        notifier.clearAlarm()

        currentState = AlarmState.Idle
        return AlarmStopResult.Stopped(
            previousState = activeState,
            reason = reason,
        )
    }
}
