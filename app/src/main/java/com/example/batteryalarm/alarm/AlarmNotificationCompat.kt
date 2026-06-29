package com.example.batteryalarm.alarm

import android.content.Context
import android.os.Bundle
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import com.example.batteryalarm.R
import com.example.batteryalarm.ui.theme.BrandGreen

internal fun alarmNotificationBuilder(
    context: Context,
    channelId: String,
    accentColor: Int = BrandGreen.toArgb(),
): NotificationCompat.Builder = NotificationCompat.Builder(context, channelId)
    .setSmallIcon(R.drawable.ic_alarm_notification)
    .setColor(accentColor)
    .addExtras(
        Bundle().apply {
            putBoolean("android.app.preferSmallIcon", true)
        },
    )
