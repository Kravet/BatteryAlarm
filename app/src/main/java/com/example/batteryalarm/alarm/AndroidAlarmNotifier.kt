package com.example.batteryalarm.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.batteryalarm.R
import com.example.batteryalarm.domain.AlarmNotifier
import com.example.batteryalarm.domain.AlarmStartReason
import com.example.batteryalarm.ui.AlarmActivity

class AndroidAlarmNotifier(
    private val context: Context,
) : AlarmNotifier {
    private val notificationManager =
        context.getSystemService(NotificationManager::class.java)

    override fun showAlarmStarted(reason: AlarmStartReason) {
        createChannelIfNeeded()

        val fullScreenIntent = AlarmActivity.createAlarmIntent(context)
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_FULL_SCREEN,
            fullScreenIntent,
            pendingIntentFlags(),
        )

        val dismissIntent = Intent(context, AlarmDismissReceiver::class.java)
        val dismissPendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_DISMISS,
            dismissIntent,
            pendingIntentFlags(),
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm_notification)
            .setContentTitle(context.getString(R.string.alarm_notification_title))
            .setContentText(context.getString(R.string.alarm_notification_body))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setContentIntent(fullScreenPendingIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(
                0,
                context.getString(R.string.dismiss),
                dismissPendingIntent,
            )
            .build()

        notificationManager.notify(ALARM_NOTIFICATION_ID, notification)
        Log.d(TAG, "Posted alarm notification, reason=$reason")
    }

    override fun clearAlarm() {
        notificationManager.cancel(ALARM_NOTIFICATION_ID)
    }

    private fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.alarm_notification_title),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = context.getString(R.string.alarm_notification_body)
            setBypassDnd(true)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
        }
        notificationManager.createNotificationChannel(channel)
    }

    private fun pendingIntentFlags(): Int {
        val immutableFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }
        return PendingIntent.FLAG_UPDATE_CURRENT or immutableFlag
    }

    companion object {
        private const val TAG = "AndroidAlarmNotifier"
        const val CHANNEL_ID = "battery_alarm"
        const val ALARM_NOTIFICATION_ID = 1001

        private const val REQUEST_FULL_SCREEN = 2001
        private const val REQUEST_DISMISS = 2002
    }
}
