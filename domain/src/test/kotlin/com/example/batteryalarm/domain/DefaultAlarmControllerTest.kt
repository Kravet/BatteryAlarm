package com.example.batteryalarm.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultAlarmControllerTest {

    @Test
    fun `when alarm is enabled, then startAlarm activates alarm and starts outputs`() = runTest {
        val settingsRepository = FakeAlarmSettingsRepository(enabled = true)
        val soundPlayer = FakeAlarmSoundPlayer()
        val vibrator = FakeAlarmVibrator()
        val notifier = FakeAlarmNotifier()
        val controller = createController(
            settingsRepository = settingsRepository,
            soundPlayer = soundPlayer,
            vibrator = vibrator,
            notifier = notifier,
        )

        val result = controller.startAlarm(AlarmStartReason.SystemLowBattery)

        val expectedState = AlarmState.Active(AlarmStartReason.SystemLowBattery)
        assertEquals(AlarmStartResult.Started(expectedState), result)
        assertEquals(expectedState, controller.state)
        assertEquals(1, soundPlayer.startLoopingCount)
        assertEquals(1, vibrator.startLoopingCount)
        assertEquals(listOf(AlarmStartReason.SystemLowBattery), notifier.startedReasons)
    }

    @Test
    fun `when alarm is disabled, then startAlarm leaves alarm idle and does not start outputs`() = runTest {
        val soundPlayer = FakeAlarmSoundPlayer()
        val vibrator = FakeAlarmVibrator()
        val notifier = FakeAlarmNotifier()
        val controller = createController(
            settingsRepository = FakeAlarmSettingsRepository(enabled = false),
            soundPlayer = soundPlayer,
            vibrator = vibrator,
            notifier = notifier,
        )

        val result = controller.startAlarm(AlarmStartReason.SystemLowBattery)

        assertEquals(AlarmStartResult.Disabled, result)
        assertEquals(AlarmState.Idle, controller.state)
        assertEquals(0, soundPlayer.startLoopingCount)
        assertEquals(0, vibrator.startLoopingCount)
        assertEquals(emptyList<AlarmStartReason>(), notifier.startedReasons)
    }

    @Test
    fun `when alarm is disabled, then test alarm still activates alarm and starts outputs`() = runTest {
        val soundPlayer = FakeAlarmSoundPlayer()
        val vibrator = FakeAlarmVibrator()
        val notifier = FakeAlarmNotifier()
        val controller = createController(
            settingsRepository = FakeAlarmSettingsRepository(enabled = false),
            soundPlayer = soundPlayer,
            vibrator = vibrator,
            notifier = notifier,
        )

        val result = controller.startAlarm(AlarmStartReason.TestAlarmFlow)

        val expectedState = AlarmState.Active(AlarmStartReason.TestAlarmFlow)
        assertEquals(AlarmStartResult.Started(expectedState), result)
        assertEquals(expectedState, controller.state)
        assertEquals(1, soundPlayer.startLoopingCount)
        assertEquals(1, vibrator.startLoopingCount)
        assertEquals(listOf(AlarmStartReason.TestAlarmFlow), notifier.startedReasons)
    }

    @Test
    fun `when alarm is already active, then startAlarm keeps existing alarm and does not restart outputs`() = runTest {
        val soundPlayer = FakeAlarmSoundPlayer()
        val vibrator = FakeAlarmVibrator()
        val notifier = FakeAlarmNotifier()
        val controller = createController(
            soundPlayer = soundPlayer,
            vibrator = vibrator,
            notifier = notifier,
        )

        controller.startAlarm(AlarmStartReason.SystemLowBattery)
        val result = controller.startAlarm(AlarmStartReason.TestAlarmFlow)

        val expectedState = AlarmState.Active(AlarmStartReason.SystemLowBattery)
        assertEquals(AlarmStartResult.AlreadyActive(expectedState), result)
        assertEquals(expectedState, controller.state)
        assertEquals(1, soundPlayer.startLoopingCount)
        assertEquals(1, vibrator.startLoopingCount)
        assertEquals(listOf(AlarmStartReason.SystemLowBattery), notifier.startedReasons)
    }

    @Test
    fun `when active alarm is dismissed by user, then stopAlarm stops outputs and returns to idle`() = runTest {
        val soundPlayer = FakeAlarmSoundPlayer()
        val vibrator = FakeAlarmVibrator()
        val notifier = FakeAlarmNotifier()
        val controller = createController(
            soundPlayer = soundPlayer,
            vibrator = vibrator,
            notifier = notifier,
        )
        controller.startAlarm(AlarmStartReason.TestAlarmFlow)

        val result = controller.stopAlarm(AlarmStopReason.UserDismissed)

        val previousState = AlarmState.Active(AlarmStartReason.TestAlarmFlow)
        assertEquals(
            AlarmStopResult.Stopped(
                previousState = previousState,
                reason = AlarmStopReason.UserDismissed,
            ),
            result,
        )
        assertEquals(AlarmState.Idle, controller.state)
        assertEquals(1, soundPlayer.stopCount)
        assertEquals(1, vibrator.stopCount)
        assertEquals(1, notifier.clearCount)
    }

    @Test
    fun `when active alarm stops because power is connected, then stopAlarm stops outputs and returns to idle`() = runTest {
        val soundPlayer = FakeAlarmSoundPlayer()
        val vibrator = FakeAlarmVibrator()
        val notifier = FakeAlarmNotifier()
        val controller = createController(
            soundPlayer = soundPlayer,
            vibrator = vibrator,
            notifier = notifier,
        )
        controller.startAlarm(AlarmStartReason.SystemLowBattery)

        val result = controller.stopAlarm(AlarmStopReason.PowerConnected)

        val previousState = AlarmState.Active(AlarmStartReason.SystemLowBattery)
        assertEquals(
            AlarmStopResult.Stopped(
                previousState = previousState,
                reason = AlarmStopReason.PowerConnected,
            ),
            result,
        )
        assertEquals(AlarmState.Idle, controller.state)
        assertEquals(1, soundPlayer.stopCount)
        assertEquals(1, vibrator.stopCount)
        assertEquals(1, notifier.clearCount)
    }

    @Test
    fun `when alarm is already idle, then stopAlarm leaves alarm idle and does not stop outputs`() {
        val soundPlayer = FakeAlarmSoundPlayer()
        val vibrator = FakeAlarmVibrator()
        val notifier = FakeAlarmNotifier()
        val controller = createController(
            soundPlayer = soundPlayer,
            vibrator = vibrator,
            notifier = notifier,
        )

        val result = controller.stopAlarm(AlarmStopReason.UserDismissed)

        assertEquals(AlarmStopResult.AlreadyIdle, result)
        assertEquals(AlarmState.Idle, controller.state)
        assertEquals(0, soundPlayer.stopCount)
        assertEquals(0, vibrator.stopCount)
        assertEquals(0, notifier.clearCount)
    }

    private fun createController(
        settingsRepository: AlarmSettingsRepository = FakeAlarmSettingsRepository(enabled = true),
        soundPlayer: FakeAlarmSoundPlayer = FakeAlarmSoundPlayer(),
        vibrator: FakeAlarmVibrator = FakeAlarmVibrator(),
        notifier: FakeAlarmNotifier = FakeAlarmNotifier(),
    ): DefaultAlarmController = DefaultAlarmController(
        settingsRepository = settingsRepository,
        soundPlayer = soundPlayer,
        vibrator = vibrator,
        notifier = notifier,
    )
}

private class FakeAlarmSettingsRepository(
    enabled: Boolean,
) : AlarmSettingsRepository {
    private val alarmEnabledFlow = MutableStateFlow(enabled)

    override val alarmEnabled: Flow<Boolean> = alarmEnabledFlow.asStateFlow()

    override suspend fun isAlarmEnabled(): Boolean = alarmEnabledFlow.value

    override suspend fun setAlarmEnabled(enabled: Boolean) {
        alarmEnabledFlow.value = enabled
    }

    override suspend fun toggleAlarmEnabled() {
        setAlarmEnabled(!isAlarmEnabled())
    }
}

private class FakeAlarmSoundPlayer : AlarmSoundPlayer {
    var startLoopingCount = 0
        private set
    var stopCount = 0
        private set

    override fun startLooping() {
        startLoopingCount += 1
    }

    override fun stop() {
        stopCount += 1
    }
}

private class FakeAlarmVibrator : AlarmVibrator {
    var startLoopingCount = 0
        private set
    var stopCount = 0
        private set

    override fun startLooping() {
        startLoopingCount += 1
    }

    override fun stop() {
        stopCount += 1
    }
}

private class FakeAlarmNotifier : AlarmNotifier {
    val startedReasons = mutableListOf<AlarmStartReason>()
    var clearCount = 0
        private set

    override fun showAlarmStarted(reason: AlarmStartReason) {
        startedReasons += reason
    }

    override fun clearAlarm() {
        clearCount += 1
    }
}
