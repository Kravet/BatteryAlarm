package com.example.batteryalarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
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
        Log.d(TAG, "User dismissed alarm")
        alarmController.stopAlarm(AlarmStopReason.UserDismissed)
        context.startActivity(AlarmActivity.createFinishIntent(context))
    }

    private companion object {
        const val TAG = "AlarmDismissReceiver"
    }
}
