# Next Steps

---

# TODO

- [Alarm Intent] Optional later: use `AlarmManager.setAlarmClock()` + `AlarmClockInfo` show intent for stronger Doze delivery and the status-bar alarm indicator. This complements FSI; it does not replace it.
- [Charging Behavior] When alarm monitoring is enabled and the battery is already critically low, show the alarm immediately because the low-battery broadcast may not fire again.
- [Alarm Experience] Improve alarm presentation polish: vibration, sound behavior, and clearer colored action buttons.
- [TESTING] Implement `AlarmPersistenceE2eTest` for reboot and process-kill scenarios (currently `@Ignore`; process-kill needs separate instrumentation process or multi-process service).
- [Release] Declare `foregroundServiceType=specialUse` in Google Play Console before production release.
- [Architecture] Improve the presentation layer with MVVM or MVP.
- [Later] Add CI/CD when the project is ready for automated checks and release packaging.
- [Release] Prepare the first release build.
- [Release] Verify the alarm flow on a physical device.
- [Release] Check permissions, battery state behavior, and notification/alarm behavior on supported Android versions.
- [Release] Write short release notes.

---

# DONE

- [Project Foundation] Created the initial project structure.
- [Domain Logic] Completed the domain alarm core.
- [Domain Logic] Formatted and renamed domain alarm tests using the when-then convention.
- [Domain Logic] Moved alarm settings coordination into domain (`BatteryLowMonitoring`, `DefaultAlarmSettingsCoordinator`).
- [Settings and Receiver] Added the battery alarm setting toggle.
- [Settings and Receiver] Exposed alarm settings as a Flow.
- [Alarm Intent] Replaced plain `startActivity()` with ongoing notification + full-screen intent (`AlarmActivity`, `USE_FULL_SCREEN_INTENT`, `showWhenLocked`, `turnScreenOn`).
- [Alarm Intent] Added dismiss action on notification and in-app alarm overlay.
- [Monitoring] Replaced manifest `BATTERY_LOW` receiver with `BatteryLowMonitoringService` (foreground service + dynamic receiver).
- [Monitoring] Restored monitoring after reboot via `BootCompletedReceiver` and app coordinator startup.
- [Charging Behavior] Stop the alarm when `ACTION_POWER_CONNECTED` is received while monitoring is active.
- [TESTING] Implemented the battery alarm UI.
- [TESTING] Added coverage for the alarm flow.
- [TESTING] Manually tested that the alarm fires correctly and wakes the device from Doze mode.
- [TESTING] Added end-to-end coverage for low-battery scenarios (enabled/disabled × foreground/background/doze) via `LowBatteryE2eTest`.
- [TESTING] Added end-to-end coverage for the test-alarm flow via `TestAlarmE2eTest`.
- [TESTING] Extracted shared E2E helpers (`E2eTestActions`, `DeviceTestHelpers`) and logcat-based sync for alarm pipeline assertions.
- [TESTING] Added debug logs across the battery alarm flow for E2E diagnosis.
