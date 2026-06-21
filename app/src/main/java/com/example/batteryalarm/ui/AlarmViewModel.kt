package com.example.batteryalarm.ui

import androidx.lifecycle.ViewModel
import com.example.batteryalarm.domain.AlarmController
import com.example.batteryalarm.domain.AlarmState
import com.example.batteryalarm.domain.AlarmStopReason
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val alarmController: AlarmController,
) : ViewModel() {
    fun isAlarmActive(): Boolean = alarmController.state is AlarmState.Active

    fun onDismissAlarmClick() {
        alarmController.stopAlarm(AlarmStopReason.UserDismissed)
    }
}
