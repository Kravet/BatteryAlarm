#!/usr/bin/env bash
set -euo pipefail

chmod +x ./gradlew
adb shell svc power stayon true

# Capture logs while the emulator is still available, including failed tests.
capture_logcat() {
  timeout 20s adb logcat -d > "${RUNNER_TEMP:-/tmp}/emulator-logcat.txt" || true
}
trap capture_logcat EXIT

if [[ "${FULL_E2E:-false}" == "true" ]]; then
  ./gradlew --no-daemon :app:connectedDebugAndroidTest
else
  # Fast PR smoke tests cover both real low-battery broadcasts and test alarm.
  ./gradlew --no-daemon :app:connectedDebugAndroidTest \
    -Pandroid.testInstrumentationRunnerArguments.class=com.example.batteryalarm.LowBatteryE2eTest,com.example.batteryalarm.TestAlarmE2eTest
fi
