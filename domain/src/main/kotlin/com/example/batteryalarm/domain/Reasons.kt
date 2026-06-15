package com.example.batteryalarm.domain

enum class AlarmStartReason {
    SystemLowBattery,
    TestAlarmFlow,
}

enum class AlarmStopReason {
    PowerConnected,
    SettingsDisabled,
    UserDismissed,
}
