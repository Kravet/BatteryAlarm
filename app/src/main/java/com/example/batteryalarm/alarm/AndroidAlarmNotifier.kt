package com.example.batteryalarm.alarm

import android.content.Context
import com.example.batteryalarm.domain.AlarmNotifier
import com.example.batteryalarm.domain.AlarmStartReason

class AndroidAlarmNotifier(
    private val context: Context,
) : AlarmNotifier {
    override fun showAlarmStarted(reason: AlarmStartReason) = Unit

    override fun clearAlarm() = Unit
}
