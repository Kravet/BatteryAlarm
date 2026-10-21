package com.example.batteryalarm.alarm

import android.content.Context
import android.util.Log
import com.example.batteryalarm.di.ApplicationScope
import com.example.batteryalarm.domain.AlarmController
import com.example.batteryalarm.domain.AlarmStartReason
import com.example.batteryalarm.domain.AlarmStartResult
import com.example.batteryalarm.domain.AlarmStopReason
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Singleton
class BatteryLowAlarmHandler @Inject constructor(
    private val alarmController: AlarmController,
    @param:ApplicationScope private val scope: CoroutineScope,
) {
    fun handleBatteryLow(context: Context, onFinished: () -> Unit) {
        Log.d(TAG, "Handling battery low event")
        scope.launch {
            runAlarmEvent(
                onFailure = { exception ->
                    Log.e(TAG, "Failed to start low-battery alarm", exception)
                },
                onFinished = onFinished,
            ) {
                triggerAlarm(AlarmStartReason.SystemLowBattery)
            }
        }
    }

    fun handlePowerConnected(onFinished: () -> Unit) {
        Log.d(TAG, "Handling power connected event")
        scope.launch {
            runAlarmEvent(
                onFailure = { exception ->
                    Log.e(TAG, "Failed to stop alarm after power connection", exception)
                },
                onFinished = onFinished,
            ) {
                val result = alarmController.stopAlarm(AlarmStopReason.PowerConnected)
                Log.d(TAG, "stopAlarm reason=PowerConnected result=$result")
            }
        }
    }

    fun handleTestAlarm(
        onTick: (secondsRemaining: Int) -> Unit = {},
        onFinished: () -> Unit = {},
        onFailed: () -> Unit = {},
    ) {
        scope.launch {
            try {
                for (secondsRemaining in TEST_ALARM_DELAY_SECONDS downTo 1) {
                    onTick(secondsRemaining)
                    delay(TEST_ALARM_TICK_MS)
                }
                triggerAlarm(AlarmStartReason.TestAlarmFlow)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                onFailed()
            } finally {
                onFinished()
            }
        }
    }

    private suspend fun triggerAlarm(reason: AlarmStartReason) {
        val result = alarmController.startAlarm(reason)
        Log.d(TAG, "startAlarm reason=$reason result=$result")
        when (result) {
            is AlarmStartResult.Started,
            is AlarmStartResult.AlreadyActive,
            AlarmStartResult.Disabled,
            AlarmStartResult.Superseded,
            -> Unit
        }
    }

    companion object {
        const val TEST_ALARM_DELAY_SECONDS = 4
        const val TEST_ALARM_TICK_MS = 1_000L
        const val TEST_ALARM_DELAY_MS = TEST_ALARM_DELAY_SECONDS * TEST_ALARM_TICK_MS
        private const val TAG = "BatteryLowAlarmHandler"
    }
}

/**
 * A broadcast's pending result must be finished even if an alarm output fails.
 * Cancellation is propagated; operational failures are reported without crashing the app.
 */
internal suspend fun runAlarmEvent(
    onFailure: (Exception) -> Unit,
    onFinished: () -> Unit,
    operation: suspend () -> Unit,
) {
    try {
        operation()
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        onFailure(exception)
    } finally {
        onFinished()
    }
}
