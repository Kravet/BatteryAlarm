# Next Steps

## Next

**Current Validation**
- Manually test that the alarm fires correctly and wakes the device from Doze mode.
- Add or run end-to-end coverage for the alarm flow, including wake-from-Doze behavior where possible.

**Charging Behavior**
- Stop the alarm immediately when the charging cable is connected.
- Stop the alarm only when real charging starts, once the app has a reliable way to detect that state.
- When alarm monitoring is enabled and the battery is already critically low, show the alarm immediately because the low-battery receiver may not fire again.

**Alarm Experience**
- Improve alarm presentation polish: vibration, sound behavior, and clearer colored action buttons.

**Architecture**
- Improve the presentation layer with MVVM or MVP.

**Stability**
- Stabilize the app with end-to-end tests.

## Later

- Add CI/CD when the project is ready for automated checks and release packaging.

## Release

- Prepare the first release build.
- Verify the alarm flow on a physical device.
- Check permissions, battery state behavior, and notification/alarm behavior on supported Android versions.
- Write short release notes.

## Done

**Project Foundation**
- Created the initial project structure.

**Domain Logic**
- Completed the domain alarm core.
- Formatted and renamed domain alarm tests using the when-then convention.

**Settings and Receiver**
- Added the battery alarm setting toggle.
- Exposed alarm settings as a Flow.
- Registered the low-battery receiver.

**UI and Testing**
- Implemented the battery alarm UI.
- Added coverage for the alarm flow.
