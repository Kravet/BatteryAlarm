package com.example.batteryalarm.alarm

import android.content.Context
import com.example.batteryalarm.di.ApplicationScope
import com.example.batteryalarm.domain.AlarmController
import com.example.batteryalarm.domain.AlarmStartReason
import com.example.batteryalarm.domain.AlarmStartResult
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
        when (alarmController.startAlarm(reason)) {
            is AlarmStartResult.Started,
            is AlarmStartResult.AlreadyActive,
            AlarmStartResult.Disabled,
            -> Unit
        }
    }

    companion object {
        const val TEST_ALARM_DELAY_MS = 5_000L
    }
}
