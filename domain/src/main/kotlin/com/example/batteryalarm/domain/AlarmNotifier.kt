package com.example.batteryalarm.domain

interface AlarmNotifier {
    fun showAlarmStarted(reason: AlarmStartReason)

    fun clearAlarm()
}
