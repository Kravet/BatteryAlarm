package com.example.batteryalarm.ui

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.batteryalarm.ui.theme.BatteryAlarmTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlarmActivity : ComponentActivity() {
    private val viewModel: AlarmViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureLockScreenFlags()

        if (shouldFinishImmediately(intent)) {
            Log.d(TAG, "onCreate: finishing, reason=finish_action")
            finish()
            return
        }

        if (!viewModel.isAlarmActive()) {
            Log.d(TAG, "onCreate: finishing, reason=alarm_inactive")
            finish()
            return
        }

        Log.d(TAG, "onCreate: showing alarm overlay")
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        setContent {
            BatteryAlarmTheme {
                LowBatteryAlarmScreen(
                    batteryPercentage = currentBatteryPercentage(),
                    onDismissAlarmClick = {
                        viewModel.onDismissAlarmClick()
                        finish()
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (shouldFinishImmediately(intent)) {
            finish()
        }
    }

    private fun configureLockScreenFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
    }

    private fun shouldFinishImmediately(intent: Intent?): Boolean =
        intent?.action == ACTION_FINISH

    private fun currentBatteryPercentage(): Int {
        val batteryManager = getSystemService(BatteryManager::class.java)
        val capacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        if (capacity in 0..100) {
            return capacity
        }

        val batteryStatus = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level >= 0 && scale > 0) {
            ((level * 100f) / scale).toInt().coerceIn(0, 100)
        } else {
            DEFAULT_BATTERY_PERCENTAGE
        }
    }

    companion object {
        private const val TAG = "AlarmActivity"
        private const val ACTION_FINISH = "com.example.batteryalarm.action.FINISH_ALARM"
        private const val DEFAULT_BATTERY_PERCENTAGE = 10

        fun createAlarmIntent(context: Context): Intent = Intent(context, AlarmActivity::class.java)
            .addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP,
            )

        fun createFinishIntent(context: Context): Intent = Intent(context, AlarmActivity::class.java)
            .setAction(ACTION_FINISH)
            .addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP,
            )
    }
}
