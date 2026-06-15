package com.example.batteryalarm.alarm

import android.util.Log
import com.example.batteryalarm.di.ApplicationScope
import com.example.batteryalarm.domain.AlarmController
import com.example.batteryalarm.domain.AlarmSettingsRepository
import com.example.batteryalarm.domain.AlarmStopReason
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@Singleton
class AlarmSettingsCoordinator @Inject constructor(
    private val alarmSettingsRepository: AlarmSettingsRepository,
    private val batteryLowReceiverRegistrar: BatteryLowReceiverRegistrar,
    private val alarmController: AlarmController,
    @param:ApplicationScope private val scope: CoroutineScope,
) {
    private var started = false

    @Synchronized
    fun start() {
        if (started) {
            return
        }
        started = true

        scope.launch {
            alarmSettingsRepository.alarmEnabled
                .distinctUntilChanged()
                .catch { exception ->
                    if (exception is CancellationException) {
                        throw exception
                    }
                    Log.w(TAG, "Failed to observe alarm settings", exception)
                }
                .collect { enabled ->
                    syncAlarmSetting(enabled)
                }
        }
    }

    private fun syncAlarmSetting(enabled: Boolean) {
        try {
            batteryLowReceiverRegistrar.setBatteryLowReceiverEnabled(enabled)
            if (!enabled) {
                alarmController.stopAlarm(AlarmStopReason.SettingsDisabled)
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Log.w(TAG, "Failed to sync alarm setting", exception)
        }
    }

    private companion object {
        const val TAG = "AlarmSettingsCoordinator"
    }
}
