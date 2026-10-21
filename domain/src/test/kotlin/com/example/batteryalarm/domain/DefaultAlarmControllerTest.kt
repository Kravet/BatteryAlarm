package com.example.batteryalarm.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
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


    @Test
    fun `when stop occurs while settings are loading then pending start is superseded`() = runTest {
        val settingsRead = CompletableDeferred<Unit>()
        val resumeSettingsRead = CompletableDeferred<Unit>()
        val settingsRepository = object : AlarmSettingsRepository {
            override val alarmEnabled: Flow<Boolean> = flowOf(true)
            override suspend fun isAlarmEnabled(): Boolean {
                settingsRead.complete(Unit)
                resumeSettingsRead.await()
                return true
            }
            override suspend fun setAlarmEnabled(enabled: Boolean) = Unit
            override suspend fun toggleAlarmEnabled() = Unit
        }
        val sound = FakeAlarmSoundPlayer()
        val controller = createController(settingsRepository = settingsRepository, soundPlayer = sound)

        val pendingStart = async { controller.startAlarm(AlarmStartReason.SystemLowBattery) }
        settingsRead.await()
        assertEquals(AlarmStopResult.AlreadyIdle, controller.stopAlarm(AlarmStopReason.PowerConnected))
        resumeSettingsRead.complete(Unit)

        assertEquals(AlarmStartResult.Superseded, pendingStart.await())
        assertEquals(AlarmState.Idle, controller.state)
        assertEquals(0, sound.startLoopingCount)
    }

    @Test
    fun `when startup output fails then started outputs are cleaned and state stays idle`() = runTest {
        val sound = FakeAlarmSoundPlayer()
        val vibrator = FakeAlarmVibrator()
        var clears = 0
        val notifier = object : AlarmNotifier {
            override fun showAlarmStarted(reason: AlarmStartReason) {
                throw IllegalStateException("notification unavailable")
            }
            override fun clearAlarm() {
                clears++
            }
        }
        val controller = DefaultAlarmController(
            settingsRepository = FakeAlarmSettingsRepository(enabled = true),
            soundPlayer = sound,
            vibrator = vibrator,
            notifier = notifier,
        )
        try {
            controller.startAlarm(AlarmStartReason.SystemLowBattery)
            fail("Expected notification failure")
        } catch (expected: IllegalStateException) {
            assertEquals("notification unavailable", expected.message)
        }

        assertEquals(AlarmState.Idle, controller.state)
        assertEquals(1, sound.stopCount)
        assertEquals(1, vibrator.stopCount)
        assertEquals(1, clears)
    }

    @Test
    fun `when sound stop fails then remaining outputs are stopped and state is idle`() = runTest {
        val sound = object : AlarmSoundPlayer {
            override fun startLooping() = Unit
            override fun stop() {
                throw IllegalStateException("sound cleanup failed")
            }
        }
        val vibrator = FakeAlarmVibrator()
        val notifier = FakeAlarmNotifier()
        val controller = DefaultAlarmController(
            settingsRepository = FakeAlarmSettingsRepository(enabled = true),
            soundPlayer = sound,
            vibrator = vibrator,
            notifier = notifier,
        )
        controller.startAlarm(AlarmStartReason.SystemLowBattery)

        try {
            controller.stopAlarm(AlarmStopReason.UserDismissed)
            fail("Expected sound cleanup failure")
        } catch (expected: IllegalStateException) {
            assertEquals("sound cleanup failed", expected.message)
        }
        assertEquals(AlarmState.Idle, controller.state)
        assertEquals(1, vibrator.stopCount)
        assertEquals(1, notifier.clearCount)
    }

    @Test
    fun `concurrent starts do not execute alarm outputs twice`() = runBlocking {
        val firstOutputStarted = CountDownLatch(1)
        val releaseOutput = CountDownLatch(1)
        val soundStarts = AtomicInteger(0)
        val sound = object : AlarmSoundPlayer {
            override fun startLooping() {
                soundStarts.incrementAndGet()
                firstOutputStarted.countDown()
                check(releaseOutput.await(5, TimeUnit.SECONDS)) { "Timed out waiting for startup" }
            }
            override fun stop() = Unit
        }
        val controller = DefaultAlarmController(
            settingsRepository = FakeAlarmSettingsRepository(enabled = true),
            soundPlayer = sound,
            vibrator = FakeAlarmVibrator(),
            notifier = FakeAlarmNotifier(),
        )
        val firstStart = async(Dispatchers.Default) {
            controller.startAlarm(AlarmStartReason.SystemLowBattery)
        }
        try {
            assertTrue(firstOutputStarted.await(5, TimeUnit.SECONDS))
            val secondStart = async(Dispatchers.Default) {
                controller.startAlarm(AlarmStartReason.SystemLowBattery)
            }
            releaseOutput.countDown()

            assertTrue(firstStart.await() is AlarmStartResult.Started)
            assertTrue(secondStart.await() is AlarmStartResult.AlreadyActive)
            assertEquals(1, soundStarts.get())
        } finally {
            releaseOutput.countDown()
        }
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
