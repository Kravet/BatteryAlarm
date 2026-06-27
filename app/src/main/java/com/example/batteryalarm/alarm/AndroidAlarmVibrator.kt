package com.example.batteryalarm.alarm

import android.content.Context
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import com.example.batteryalarm.domain.AlarmVibrator

class AndroidAlarmVibrator(
    context: Context,
) : AlarmVibrator {
    private val lock = Any()
    private val vibrator = context.getSystemService(Vibrator::class.java)

    override fun startLooping() {
        synchronized(lock) {
            stopLocked()
            val vibrator = vibrator ?: return
            if (!vibrator.hasVibrator()) {
                Log.w(TAG, "Device has no vibrator")
                return
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(ALARM_PATTERN, REPEAT_INDEX)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val attributes = VibrationAttributes.Builder()
                        .setUsage(VibrationAttributes.USAGE_ALARM)
                        .build()
                    vibrator.vibrate(effect, attributes)
                } else {
                    vibrator.vibrate(effect)
                }
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(ALARM_PATTERN, REPEAT_INDEX)
            }
            Log.d(TAG, "Started looping alarm vibration")
        }
    }

    override fun stop() {
        synchronized(lock) {
            stopLocked()
        }
    }

    private fun stopLocked() {
        vibrator?.cancel()
    }

    companion object {
        private const val TAG = "AndroidAlarmVibrator"
        private const val REPEAT_INDEX = 1
        private val ALARM_PATTERN = longArrayOf(0, 1_000, 1_000)
    }
}
