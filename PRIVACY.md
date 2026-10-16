# Privacy Policy — Battery Alarm

**Last updated:** 16 October 2026

This Privacy Policy describes how the **Battery Alarm** Android application (“the App”) handles information. It is based on the App’s current implementation and third-party SDK behavior as documented by those providers.

This document is provided for transparency. It is **not** legal advice and does **not** by itself establish that processing is lawful in every jurisdiction.

---

## 1. Who we are (data controller)

| Field | Value |
| --- | --- |
| Application | Battery Alarm |
| Android application ID | `com.kravets.batteryalarm` |
| Public distribution | [Kravet/battery-alarm-releases](https://github.com/Kravet/battery-alarm-releases) |
| Data controller | Andrii Kravets |
| Contact | kravetapp@gmail.com |

If you have questions about this policy or your data, contact kravetapp@gmail.com.

---

## 2. Scope

This policy covers the Battery Alarm Android app distributed as a signed APK via GitHub Releases. It does not cover GitHub’s own processing when you visit GitHub websites or download releases in a browser.

---

## 3. Summary

- The App’s core alarm features run **on your device**.
- The App uses **Firebase Analytics** and **Firebase Crashlytics** (Google). Those services transmit diagnostic and usage data to Google when the App runs.
- The App may contact **GitHub’s public API** to check for updates.
- The App does **not** currently offer an in-app switch to turn analytics or crash reporting off.
- Collection by Firebase SDKs begins when the App starts; there is **no in-app consent screen** before that collection in the current version.

---

## 4. Information processed on your device (local)

The following stay on the device for App functionality (unless included in Android system backup if you enable device backup):

| Data | Purpose |
| --- | --- |
| Alarm enabled / disabled preference | Control whether low-battery monitoring and alarms run |
| Pending update check metadata (for example last check / pending release URL) | Update checker behavior |
| Battery / charging state from Android system APIs | Detect low battery and power connection; play alarm |
| Notification permission state | Show monitoring and alarm notifications |

The App does **not** create a user account. It does **not** ask for your name, email, or phone number inside the App.

---

## 5. Information transmitted externally

### 5.1 Firebase Analytics (Google)

The App includes the Firebase Analytics SDK and logs product events such as:

| Event (as implemented) | Parameters |
| --- | --- |
| `alarm_toggled` | `enabled` (boolean) |
| `notification_permission_denied` | (none) |
| `test_alarm_started` | (none) |
| `test_alarm_failed` | (none) |
| `alarm_started` | `reason` (`system_low_battery` or `test`), `hour_of_day` (0–23, device local hour) |
| `alarm_start_blocked` | `result` (`already_active` or `disabled`) |
| `alarm_stopped` | `reason` (`power_connected`, `settings_disabled`, or `user_dismissed`), optional `duration_sec` |
| `update_available_shown` | `version` (version name string) |
| `update_clicked` | `version` (version name string) |

In the current code:

- No Firebase Analytics **user ID** is set.
- Custom event parameters use only the values above (booleans, short enum-like strings, local hour, duration, version name).
- Advertising ID (AAID) collection is **disabled** in the App manifest (`google_analytics_adid_collection_enabled` = `false`).

In addition to custom events, Firebase Analytics typically collects **automatic** data described by Google (for example app instance identifiers, app version, device/OS information, and approximate location derived from IP). See Google’s documentation: [Firebase / Google Analytics data collection](https://firebase.google.com/support/privacy) and [Configure Analytics data collection](https://firebase.google.com/docs/analytics/configure-data-collection).

### 5.2 Firebase Crashlytics (Google)

The App includes the Firebase Crashlytics SDK. The current App code does **not** set custom Crashlytics user IDs, custom keys, or custom logs.

Crashlytics **automatically** collects crash and diagnostic information when crashes occur (for example stack traces, device state at crash time, app version, and installation identifiers), as described by Google: [Firebase Crashlytics data](https://firebase.google.com/support/privacy). Default Crashlytics retention described by Google is on the order of **90 days** for crash-related data; confirm the current period in Google’s documentation.

### 5.3 GitHub update check

When the update checker runs, the App requests the latest release metadata from GitHub’s public API for `Kravet/battery-alarm-releases`. The request includes a fixed `User-Agent` (`BatteryAlarm-UpdateChecker`) and standard HTTP metadata. GitHub processes the request under GitHub’s own terms and privacy policy. The App does not send your name, email, or alarm preferences in that request.

### 5.4 Other SDKs

Aside from Android / Jetpack libraries used for UI and local storage, the main network-capable third-party SDKs in the release build are **Firebase Analytics**, **Firebase Crashlytics**, and **OkHttp** (used for the GitHub update check). No advertising SDK was found in the current dependencies.

---

## 6. Purposes of processing

| Purpose | Related processing |
| --- | --- |
| Provide low-battery alarms and monitoring | Local battery/charging state and alarm settings |
| Understand feature usage and improve the App | Firebase Analytics events and automatic analytics data |
| Diagnose crashes and improve stability | Firebase Crashlytics automatic crash reports |
| Inform you of newer APK versions | GitHub Releases API check and opening the release page in a browser |

---

## 7. Legal bases (EU / GDPR)

The App’s core battery monitoring and alarm functions operate locally on your device.

We use GitHub’s API to check for app updates, based on our legitimate interest in maintaining and improving the App (GDPR Article 6(1)(f)).

Firebase Crashlytics is used to diagnose crashes and improve stability. Its legal basis and any applicable consent requirements have not yet been fully assessed.

Firebase Analytics currently collects usage information without prior in-app consent. We have not established that this collection satisfies all applicable EU privacy requirements. This Privacy Policy does not replace any consent required by law.

---

## 8. Processors and international transfers

Google (Firebase / Google Analytics / Crashlytics) acts as a processor/service provider for analytics and crash reporting. Data may be processed on servers outside your country, including outside the EU/EEA.

Google publishes data processing terms and international transfer safeguards, including Standard Contractual Clauses and, where applicable, the EU–US Data Privacy Framework. We have not yet verified which specific mechanisms apply to all data processed through this App.

GitHub, Inc. processes update-check requests as an independent controller or service provider when you or the App contact GitHub.

---

## 9. Retention

| Data | Retention |
| --- | --- |
| Local App preferences | Until you clear App data, uninstall the App, or Android backup/restore replaces them |
| Firebase Analytics | Determined by the Google Analytics property settings, which have not yet been verified. Google documents configurable retention periods of 2 or 14 months for standard properties, depending on the data category. These settings do not determine the retention of standard aggregated reports. |
| Firebase Crashlytics | Google documents approximately 90 days for typical crash data, after which Google begins a deletion process |
| GitHub API logs | Controlled by GitHub, not by the App |

---

## 10. Your rights (EU / GDPR)

If GDPR applies to you, you may have rights to access, rectify, erase, restrict, object to certain processing, and data portability, and the right to lodge a complaint with a supervisory authority (in Poland: the President of the Personal Data Protection Office — UODO).

To exercise rights related to data the App developer controls, contact kravetapp@gmail.com.

We review privacy requests received at kravetapp@gmail.com.

The App does not maintain user accounts or a database linking email addresses to app installations.

Where technically possible, we may use Google’s available tools to request deletion of data associated with an identifiable app installation. We cannot guarantee that an installation can be identified from an email request alone or that all historical data can be individually deleted.

If we cannot complete a deletion request, we will explain the applicable limitations.

Local App data can be removed by clearing the App’s data or uninstalling it, subject to any Android device backups.

Uninstalling the App stops future local processing and typically stops new analytics/crash events from that installation, but does not by itself erase historical data already stored by Google.

---

## 11. Consent, controls, and current limitations

Current implementation facts:

1. Firebase Analytics and Crashlytics are initialized via standard Firebase / Google Services integration.
2. There is **no** in-app privacy consent dialog before collection starts.
3. There is **no** in-app setting to disable Analytics or Crashlytics.
4. Advertising ID collection is disabled in the manifest.
5. Debug and release builds use the same Firebase Analytics and Crashlytics dependencies; no separate “collection off in release” configuration was found.

**Implication for EU users:** if analytics and/or crash reporting require prior consent under applicable law, the current implementation may **not** meet that requirement until a consent or opt-in mechanism is added. See the accompanying engineering notes / product decision needed by the developer.

Practical steps users can take today:

- Stop using / uninstall the App.
- Restrict the App’s network access via system settings (if available on your device/OEM), which may also break update checks.
- Contact kravetapp@gmail.com for privacy requests.

---

## 12. Children

Battery Alarm is intended for general users and is not specifically directed at children.

---

## 13. Changes to this policy

We may update this Privacy Policy when the App’s data practices change. The “Last updated” date at the top will change when this document is revised. Material changes should be reflected in the public copy of this policy and, where appropriate, communicated in release notes.

---

## 14. Public copy of this policy

The canonical public copy is intended to remain available from the public distribution repository even if the source repository becomes private:

**https://github.com/Kravet/battery-alarm-releases/blob/main/PRIVACY.md**

(Plain text: https://raw.githubusercontent.com/Kravet/battery-alarm-releases/main/PRIVACY.md)

