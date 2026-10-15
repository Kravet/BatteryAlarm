package com.example.batteryalarm.update

import android.util.Log

object UpdateCheckLog {
    const val TAG = "UpdateCheck"

    fun d(message: String) {
        try {
            Log.d(TAG, message)
        } catch (_: RuntimeException) {
            // android.util.Log is a stub in JVM unit tests.
        }
    }

    fun w(message: String) {
        try {
            Log.w(TAG, message)
        } catch (_: RuntimeException) {
            // android.util.Log is a stub in JVM unit tests.
        }
    }
}
