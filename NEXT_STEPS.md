# Engineering Backlog

This is a **prioritized list of follow-up work**, not a list of architectural defects. The [README](README.md) describes what is already implemented. Architectural and reliability changes are deliberately separate: the codebase can be used as an engineering portfolio while broader device validation remains pending.

## Portfolio quality — high ROI

- [x] **CI verified** — `.github/workflows/android-ci.yml` runs `:domain:test`, `:app:testDebugUnitTest` and `:app:assembleDebug`. The initial verification passed on the portfolio branch; check subsequent runs for regressions.
- [ ] **P3 — Review ViewModel orchestration when complexity grows** — `MainViewModel` owns alarm, update and privacy UI workflows. Current decision: keep one screen-level ViewModel; the persisted consent repository remains the source of truth, and further collaborators would add indirection without a demonstrated need. Revisit when workflows gain a second consumer or become hard to test.
- [ ] **P2 — Check critical Android entry-point behavior** — review how the foreground service, activity and receiver report failures. Repair confirmed issues without broad restructuring.

## Architecture — intentional trade-offs

- [ ] **P3 — Optional compiler-enforced domain implementation boundary.** Current Gradle graph is `:app -> :domain` and Hilt modules in `:app` construct `DefaultAlarmController` and `DefaultAlarmSettingsCoordinator` directly. This is acceptable for a small project but relies on convention, not compiler enforcement. If it becomes valuable, consider `:domain-api`, `:domain-impl`, and `:composition` (with `:app -> :domain-api`, `:app -> :composition`, `:composition -> :domain-impl`, `:domain-impl -> :domain-api`). **Not currently implemented.**
- [ ] **P3 — Review Compose file size only for real cohesion problems.** `MainSettingsContent`, `PrivacyBottomSheet`, and `LowBatteryAlarmScreen` are sizeable, but line count alone is not a reason for more files.
- [ ] **P3 — Evaluate richer UI-specific models where domain objects leak into Compose.** Existing `MainUiState` and `AppUpdateUiModel` already provide presentation models; do not add mapping layers without a concrete need.

## Device reliability — separate pre-release track

P1 = required to close a confirmed release risk, P2 = validation/polish before broad rollout, P3 = optional. These are **not blockers for publishing the source as an engineering portfolio**.

- [ ] **P2 — Validate physical-device alarm delivery** across supported Android versions, Doze, OEM background restrictions, process death and reboot. Unit tests cannot prove system delivery.
- [ ] **P1 — Finish reboot E2E.** `AlarmPersistenceE2eTest` has an active process-kill scenario, but the reboot case is `@Ignore` and needs reboot/reconnect handling. Presence of test code does not prove execution.
- [ ] **P1 — Alarm already low when enabled.** Verified gap: enabling monitoring registers for future `ACTION_BATTERY_LOW` events but does not read the current battery level; the broadcast may not repeat. Plan a snapshot read (such as `ACTION_BATTERY_CHANGED`) and add a regression scenario without bypassing normal stop behavior.
- [ ] **P2 — Confirm error recovery of Android audio, vibration and notifications** through real failure scenarios. The domain controller now serializes transitions and attempts cleanup, but platform output operations still require runtime checks.
- [ ] **P3 — Assess `AlarmManager.setAlarmClock()` only if device testing demonstrates a delivery gap.** It is not a default architectural requirement.
- [ ] **P2 — Review permission/notification and charging behavior** on real devices, including turning monitoring on/off while an alarm is pending.

## Distribution and production readiness

- [ ] **P1 —** Run release smoke tests using the **exact signed APK** and verify package name, signing certificate and monotonically increasing `versionCode`.
- [ ] **P2 —** Verify GitHub Releases asset name, stable/prerelease metadata and update URLs. The in-app checker deliberately targets **stable releases only**.
- [ ] **P1 (Play launch) —** Confirm foreground-service `specialUse` declaration and related Google Play requirements when Play publication is planned.
- [ ] **P1 (public release) —** Validate Firebase consent and withdrawal on a device and check the written [privacy policy](PRIVACY.md) before broader distribution.
- [ ] **P2 —** Prepare release notes and a documented manual rollout/test checklist; add automation when there is a recurring release process.

## Implemented architecture (references)

- [x] Kotlin/JVM domain logic, ports and unit tests.
- [x] DataStore-backed alarm settings observed by `AlarmSettingsCoordinator`.
- [x] Foreground monitoring service with dynamically registered low-battery and power-connected receiver.
- [x] MVVM-style `MainViewModel` / `MainUiState` and Compose UI.
- [x] Analytics decorator around the domain controller.
- [x] Persisted separate Analytics and Crashlytics consent with application-scoped Firebase application.
- [x] Stable-only GitHub update checker with SemVer and attempt throttling.
- [x] Synchronization of domain alarm transitions with explicit stop-over-start handling.
- [x] Localized hold-to-stop copy aligned with the existing 1,000 ms gesture.
- [x] Unit and Android instrumentation **test sources** (not a claim that every device scenario has passed).

Avoid promoting proposals to "implemented" until corresponding code and verification exist.
