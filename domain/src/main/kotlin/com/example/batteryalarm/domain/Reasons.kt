package com.example.batteryalarm.domain

enum class AlarmStartReason {
    SystemLowBattery,
    Manual,
}

enum class AlarmStopReason {
    PowerConnected,
    UserDismissed,
}
