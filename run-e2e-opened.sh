#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

if ! adb get-state >/dev/null 2>&1; then
  echo "No Android device/emulator connected. Start Pixel_7_33 and retry." >&2
  exit 1
fi

adb shell input keyevent KEYCODE_WAKEUP >/dev/null
adb shell cmd statusbar collapse >/dev/null
adb shell wm dismiss-keyguard >/dev/null
adb shell input swipe 540 2000 540 500 100 >/dev/null

TEST_CLASS='com.example.batteryalarm.LowBatteryE2eTest#enabled_foreground'

./gradlew :app:connectedDebugAndroidTest \
  "-Pandroid.testInstrumentationRunnerArguments.class=${TEST_CLASS}"
