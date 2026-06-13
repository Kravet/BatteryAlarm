package com.example.batteryalarm.domain

class DefaultAlarmController(
    private val settingsRepository: AlarmSettingsRepository,
    private val soundPlayer: AlarmSoundPlayer,
    private val vibrator: AlarmVibrator,
    private val notifier: AlarmNotifier,
) : AlarmController {
    private var currentState = AlarmState.Idle

    override val state: AlarmState
        get() = currentState

    override fun startAlarm(reason: AlarmStartReason) {
        if (!settingsRepository.isAlarmEnabled() || currentState == AlarmState.Active) {
            return
        }

        currentState = AlarmState.Active
        soundPlayer.play()
        vibrator.vibrate()
        notifier.showAlarmStarted(reason)
    }

    override fun stopAlarm(reason: AlarmStopReason) {
        if (currentState == AlarmState.Idle) {
            return
        }

        currentState = AlarmState.Idle
        soundPlayer.stop()
        vibrator.stop()
        notifier.clearAlarm()
    }
}
