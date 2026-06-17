package com.example.batteryalarm.alarm

import android.content.Context
import com.example.batteryalarm.di.ApplicationScope
import com.example.batteryalarm.domain.AlarmController
import com.example.batteryalarm.domain.AlarmStartReason
import com.example.batteryalarm.domain.AlarmStartResult
import com.example.batteryalarm.ui.MainActivity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Singleton
class BatteryLowAlarmHandler @Inject constructor(
    private val alarmController: AlarmController,
    @param:ApplicationScope private val scope: CoroutineScope,
) {
    fun handleBatteryLow(context: Context, onFinished: () -> Unit) {
        scope.launch {
            try {
                when (alarmController.startAlarm(AlarmStartReason.SystemLowBattery)) {
                    is AlarmStartResult.Started,
                    is AlarmStartResult.AlreadyActive,
                    -> context.startActivity(MainActivity.createAlarmIntent(context))

                    AlarmStartResult.Disabled -> Unit
                }
            } catch (exception: CancellationException) {
                throw exception
            } finally {
                onFinished()
            }
        }
    }
}
