# Next Steps

---

# TODO

- [Alarm Intent] Replace plain `startActivity()` with a high-priority notification + full-screen intent in `AndroidAlarmNotifier`, so the alarm UI can appear reliably over the lock screen. Add `USE_FULL_SCREEN_INTENT`, `showWhenLocked`, and `turnScreenOn` where needed.
- What is the problem?
- Off docs https://developer.android.com/develop/ui/compose/notifications/create-notification#urgent-message
- Present expected behavior

- [Alarm Intent] Optional later: use `AlarmManager.setAlarmClock()` + `AlarmClockInfo` show intent for stronger Doze delivery and the status-bar alarm indicator. This complements FSI; it does not replace it.
- [Charging Behavior] Stop the alarm immediately when the charging cable is connected.
- [Charging Behavior] Stop the alarm only when real charging starts, once the app has a reliable way to detect that state.
- [Charging Behavior] When alarm monitoring is enabled and the battery is already critically low, show the alarm immediately because the low-battery receiver may not fire again.
- [Alarm Experience] Improve alarm presentation polish: vibration, sound behavior, and clearer colored action buttons.
- [Architecture] Improve the presentation layer with MVVM or MVP.
- [TESTING] Stabilize the app with end-to-end tests.
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
- [Settings and Receiver] Added the battery alarm setting toggle.
- [Settings and Receiver] Exposed alarm settings as a Flow.
- [Settings and Receiver] Registered the low-battery receiver.
- [TESTING] Implemented the battery alarm UI.
- [TESTING] Added coverage for the alarm flow.
- [TESTING] Manually tested that the alarm fires correctly and wakes the device from Doze mode.
- [TESTING] Added end-to-end coverage for the alarm flow, including wake-from-Doze behavior where possible.
