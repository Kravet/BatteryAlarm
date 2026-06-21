package com.example.batteryalarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.batteryalarm.domain.AlarmController
import com.example.batteryalarm.domain.AlarmStopReason
import com.example.batteryalarm.ui.AlarmActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AlarmDismissReceiver : BroadcastReceiver() {
    @Inject
    lateinit var alarmController: AlarmController

    override fun onReceive(context: Context, intent: Intent) {
        alarmController.stopAlarm(AlarmStopReason.UserDismissed)
        context.startActivity(AlarmActivity.createFinishIntent(context))
    }
}
