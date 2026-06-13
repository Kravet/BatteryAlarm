package com.example.batteryalarm.alarm

import android.content.Context
import com.example.batteryalarm.domain.AlarmVibrator

class AndroidAlarmVibrator(
    private val context: Context,
) : AlarmVibrator {
    override fun startLooping() = Unit

    override fun stop() = Unit
}
