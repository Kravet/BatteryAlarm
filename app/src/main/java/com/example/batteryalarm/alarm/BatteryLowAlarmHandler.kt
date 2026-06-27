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
            try {
                triggerAlarm(AlarmStartReason.SystemLowBattery)
            } catch (exception: CancellationException) {
                throw exception
            } finally {
                onFinished()
            }
        }
    }

    fun handlePowerConnected(onFinished: () -> Unit) {
        Log.d(TAG, "Handling power connected event")
        scope.launch {
            try {
                val result = alarmController.stopAlarm(AlarmStopReason.PowerConnected)
                Log.d(TAG, "stopAlarm reason=PowerConnected result=$result")
            } catch (exception: CancellationException) {
                throw exception
            } finally {
                onFinished()
            }
        }
    }

    fun handleTestAlarm(
        onFinished: () -> Unit = {},
        onFailed: () -> Unit = {},
    ) {
        scope.launch {
            try {
                delay(TEST_ALARM_DELAY_MS)
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
            -> Unit
        }
    }

    companion object {
        const val TEST_ALARM_DELAY_MS = 1_000L
        private const val TAG = "BatteryLowAlarmHandler"
    }
}
