package com.example.batteryalarm.alarm

import android.content.Context
import com.example.batteryalarm.domain.AlarmSoundPlayer

class AndroidAlarmSoundPlayer(
    private val context: Context,
) : AlarmSoundPlayer {
    override fun startLooping() = Unit

    override fun stop() = Unit
}
