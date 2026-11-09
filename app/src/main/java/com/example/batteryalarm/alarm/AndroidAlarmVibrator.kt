package com.example.batteryalarm.alarm

import android.content.Context
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import com.example.batteryalarm.analytics.AppAnalytics
import com.example.batteryalarm.analytics.recordSafely
import com.example.batteryalarm.domain.AlarmVibrator

class AndroidAlarmVibrator(
    context: Context,
    private val analytics: AppAnalytics,
) : AlarmVibrator {
    private val lock = Any()
    private val vibrator = context.getSystemService(Vibrator::class.java)

    override fun startLooping() {
        synchronized(lock) {
            stopLocked()
            val vibrator = vibrator ?: run {
                analytics.recordSafely {
                    logAlarmOutputResult(OUTPUT_VIBRATION, STATUS_UNAVAILABLE)
                }
                return
            }
            if (!vibrator.hasVibrator()) {
                Log.w(TAG, "Device has no vibrator")
                analytics.recordSafely {
                    logAlarmOutputResult(OUTPUT_VIBRATION, STATUS_UNAVAILABLE)
                }
                return
            }

            try {
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
                analytics.recordSafely {
                    logAlarmOutputResult(OUTPUT_VIBRATION, STATUS_REQUEST_SUBMITTED)
                }
            } catch (exception: Exception) {
                analytics.recordSafely {
                    logAlarmOutputResult(OUTPUT_VIBRATION, STATUS_ERROR)
                }
                throw exception
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
        private const val OUTPUT_VIBRATION = "vibration"
        private const val STATUS_UNAVAILABLE = "unavailable"
        private const val STATUS_REQUEST_SUBMITTED = "request_submitted"
        private const val STATUS_ERROR = "error"
        private const val REPEAT_INDEX = 1
        private val ALARM_PATTERN = longArrayOf(0, 1_000, 1_000)
    }
}
