package com.example.batteryalarm.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.batteryalarm.R
import com.example.batteryalarm.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BatteryLowMonitoringService : Service() {
    @Inject
    lateinit var batteryLowAlarmHandler: BatteryLowAlarmHandler

    private var batteryEventReceiver: BroadcastReceiver? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service onCreate")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand action=${intent?.action} startId=$startId")
        startMonitoring()
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d(TAG, "Service onDestroy")
        unregisterBatteryEventReceiver()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        super.onDestroy()
    }

    private fun startMonitoring() {
        createChannelIfNeeded()
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            REQUEST_CONTENT,
            MainActivity.createIntent(this),
            pendingIntentFlags(),
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm_notification)
            .setContentTitle(getString(R.string.monitoring_notification_title))
            .setContentText(getString(R.string.monitoring_notification_body))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(contentPendingIntent)
            .build()
        startForeground(NOTIFICATION_ID, notification)
        registerBatteryEventReceiver()
        Log.d(TAG, "Battery low monitoring started")
    }

    private fun registerBatteryEventReceiver() {
        if (batteryEventReceiver != null) {
            Log.d(TAG, "Battery event receiver already registered, skipping")
            return
        }

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    Intent.ACTION_BATTERY_LOW -> {
                        Log.d(TAG, "ACTION_BATTERY_LOW received")
                        val pendingResult = goAsync()
                        batteryLowAlarmHandler.handleBatteryLow(context) {
                            pendingResult.finish()
                        }
                    }
                    Intent.ACTION_POWER_CONNECTED -> {
                        Log.d(TAG, "ACTION_POWER_CONNECTED received")
                        val pendingResult = goAsync()
                        batteryLowAlarmHandler.handlePowerConnected {
                            pendingResult.finish()
                        }
                    }
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_LOW)
            addAction(Intent.ACTION_POWER_CONNECTED)
        }
        ContextCompat.registerReceiver(
            this,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        batteryEventReceiver = receiver
        Log.d(TAG, "Battery event receiver registered")
    }

    private fun unregisterBatteryEventReceiver() {
        val receiver = batteryEventReceiver ?: return
        unregisterReceiver(receiver)
        batteryEventReceiver = null
        Log.d(TAG, "Battery event receiver unregistered")
    }

    private fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.monitoring_notification_title),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = getString(R.string.monitoring_notification_body)
            setShowBadge(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
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
        private const val TAG = "BatteryLowMonitoringSvc"
        const val ACTION_START = "com.example.batteryalarm.action.START_BATTERY_LOW_MONITORING"
        const val CHANNEL_ID = "battery_low_monitoring"
        const val NOTIFICATION_ID = 1002
        private const val REQUEST_CONTENT = 3001

        fun startIntent(context: Context): Intent =
            Intent(context, BatteryLowMonitoringService::class.java).apply {
                action = ACTION_START
            }

        fun serviceIntent(context: Context): Intent =
            Intent(context, BatteryLowMonitoringService::class.java)
    }
}
