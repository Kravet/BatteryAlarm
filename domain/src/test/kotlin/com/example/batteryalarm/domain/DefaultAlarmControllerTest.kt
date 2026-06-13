package com.example.batteryalarm.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultAlarmControllerTest {
    @Test
    fun startAlarmActivatesAlarmAndStopAlarmReturnsToIdle() {
        val soundPlayer = FakeAlarmSoundPlayer()
        val vibrator = FakeAlarmVibrator()
        val notifier = FakeAlarmNotifier()
        val controller = DefaultAlarmController(
            settingsRepository = FakeAlarmSettingsRepository(enabled = true),
            soundPlayer = soundPlayer,
            vibrator = vibrator,
            notifier = notifier,
        )

        controller.startAlarm(AlarmStartReason.SystemLowBattery)

        assertEquals(AlarmState.Active, controller.state)
        assertEquals(1, soundPlayer.playCount)
        assertEquals(1, vibrator.vibrateCount)
        assertEquals(1, notifier.showCount)

        controller.stopAlarm(AlarmStopReason.UserDismissed)

        assertEquals(AlarmState.Idle, controller.state)
        assertEquals(1, soundPlayer.stopCount)
        assertEquals(1, vibrator.stopCount)
        assertEquals(1, notifier.clearCount)
    }

    @Test
    fun startAlarmDoesNothingWhenAlarmIsDisabled() {
        val soundPlayer = FakeAlarmSoundPlayer()
        val controller = DefaultAlarmController(
            settingsRepository = FakeAlarmSettingsRepository(enabled = false),
            soundPlayer = soundPlayer,
            vibrator = FakeAlarmVibrator(),
            notifier = FakeAlarmNotifier(),
        )

        controller.startAlarm(AlarmStartReason.SystemLowBattery)

        assertEquals(AlarmState.Idle, controller.state)
        assertEquals(0, soundPlayer.playCount)
    }
}

private class FakeAlarmSettingsRepository(
    private val enabled: Boolean,
) : AlarmSettingsRepository {
    override fun isAlarmEnabled(): Boolean = enabled
}

private class FakeAlarmSoundPlayer : AlarmSoundPlayer {
    var playCount = 0
        private set
    var stopCount = 0
        private set

    override fun play() {
        playCount += 1
    }

    override fun stop() {
        stopCount += 1
    }
}

private class FakeAlarmVibrator : AlarmVibrator {
    var vibrateCount = 0
        private set
    var stopCount = 0
        private set

    override fun vibrate() {
        vibrateCount += 1
    }

    override fun stop() {
        stopCount += 1
    }
}

private class FakeAlarmNotifier : AlarmNotifier {
    var showCount = 0
        private set
    var clearCount = 0
        private set

    override fun showAlarmStarted(reason: AlarmStartReason) {
        showCount += 1
    }

    override fun clearAlarm() {
        clearCount += 1
    }
}
