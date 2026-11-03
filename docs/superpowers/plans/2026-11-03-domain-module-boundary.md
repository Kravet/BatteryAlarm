# Domain Module Boundary Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the mixed `:domain` dependency with `:domain-api`, `:domain-impl`, and `:domain-di`, leaving `:app` with compile dependencies only on API and Hilt composition.

**Architecture:** Keep public contracts and cross-layer models in an Android/Hilt-free Kotlin/JVM API module; move concrete rules and tests into a Kotlin/JVM implementation module; add an Android library module for Hilt bindings. `:domain-di` depends on implementation with Gradle `implementation`, so implementation classes stay off the app compile classpath while remaining available at runtime.

**Tech Stack:** Kotlin 2.4.0, AGP 9.2.1 built-in Kotlin, Gradle 9.4.1, Hilt 2.59.2, KSP 2.3.12, coroutines 1.10.1.

**Spec:** `docs/superpowers/specs/2026-11-03-domain-module-boundary-design.md`

## Global Constraints

- Preserve `:app` behavior, Hilt singleton scopes, update throttle/cancellation/URL-validation semantics, and analytics decoration.
- Keep `:domain-api` free of Android and Hilt dependencies; keep `:domain-impl` free of app/Android dependencies.
- Keep Android adapters and `BuildConfig`-based update configuration in `:app`; `:domain-di` must not depend on `:app`.
- Do not change language/plugin/library versions or add a factory-only test that imports implementation into app tests.
- Do not commit intermediate states; create one implementation commit on top of `8d448ac` after final review and full verification.

## Review Focus

- **Hilt aggregation:** the app component discovers `:domain-di` from its transitive dependencies and resolves both raw/decorated controller bindings. Verify with app Hilt compilation and `:app:assembleDebug`.
- **Compile boundary:** `debugCompileClasspath` contains `:domain-api` and `:domain-di`, but no `:domain-impl` or legacy `:domain`. Verify with `.github/scripts/verify-domain-boundary.sh`.
- **Sound-session lifetime:** the injected `AlarmSoundSession` remains singleton and uses the app-provided `@ApplicationScope`; existing sound-session tests and full instrumentation cover behavior.
- **Update policy:** `AppUpdateChecker` keeps the existing cadence/results, while app-provided `ReleasePageUrlPolicy` is reused by both checking and opening. Verify implementation tests and app feature tests with API fakes.
- **API leakage:** every type referenced by app production Kotlin is in `:domain-api` or `:domain-di`; source search and app compilation verify no implementation imports remain.

---

### Task 1: Introduce app-facing update contracts with tests first

**Files:**
- Modify: `app/src/test/java/com/example/batteryalarm/ui/feature/UpdateFeatureTest.kt`
- Modify: `app/src/test/java/com/example/batteryalarm/ui/MainViewModelTest.kt`
- Modify: `app/src/test/java/com/example/batteryalarm/ui/UpdateCheckUiScenarioTest.kt`
- Create: `domain-api/src/main/kotlin/com/example/batteryalarm/domain/update/AppUpdateChecker.kt`
- Modify: `domain-impl/src/main/kotlin/com/example/batteryalarm/domain/update/CheckForAppUpdate.kt`
- Modify: `domain-impl/src/main/kotlin/com/example/batteryalarm/domain/AlarmDomainFactory.kt`
- Modify: `app/src/main/java/com/example/batteryalarm/ui/feature/UpdateFeature.kt`
- Modify: `app/src/main/java/com/example/batteryalarm/di/UpdateModule.kt`

- [x] Replace app presentation tests' use-case constructor helpers with `AppUpdateChecker` fakes; observed RED on missing API (`:app:testDebugUnitTest`).
- [x] Add the `AppUpdateChecker` and `ReleasePageUrlPolicy` API contracts; make `CheckForAppUpdate` implement the API and consume the policy.
- [x] Change `UpdateFeature` to depend on `AppUpdateChecker`; adapt app tests to API fakes while retaining use-case algorithm assertions in the implementation test suite.
- [x] Extend `AlarmDomainFactory`; provide one `ReleasePageUrlPolicy` used by checking and `AndroidUpdateOpener`.
- [x] Run `./gradlew --no-daemon :domain:test :app:testDebugUnitTest`; all tests passed before the module split.

### Task 2: Put sound-session construction behind an API interface

**Files:**
- Modify: `domain-api/src/main/kotlin/com/example/batteryalarm/domain/AlarmSoundSession.kt`
- Create: `domain-impl/src/main/kotlin/com/example/batteryalarm/domain/DefaultAlarmSoundSession.kt`
- Modify: `domain-impl/src/main/kotlin/com/example/batteryalarm/domain/VolumeRampController.kt`
- Modify: `domain-impl/src/main/kotlin/com/example/batteryalarm/domain/VolumeRampConfig.kt`
- Modify: `domain-impl/src/main/kotlin/com/example/batteryalarm/domain/VolumeRampCurve.kt`
- Modify: `domain-impl/src/main/kotlin/com/example/batteryalarm/domain/AlarmDomainFactory.kt`
- Modify: `domain-impl/src/test/kotlin/com/example/batteryalarm/domain/AlarmSoundSessionTest.kt`
- Modify: `app/src/main/java/com/example/batteryalarm/alarm/AndroidAlarmSoundPlayer.kt`
- Modify: `app/src/main/java/com/example/batteryalarm/di/AlarmModule.kt`

- [x] Change `AlarmSoundSessionTest` to exercise `DefaultAlarmSoundSession`; observed RED because that class did not exist.
- [x] Make `AlarmSoundSession` the API interface; move synchronized behavior to internal `DefaultAlarmSoundSession` and keep volume-ramp types internal.
- [x] Add `AlarmDomainFactory.createAlarmSoundSession(scope)`; provide the singleton through Hilt and inject it into `AndroidAlarmSoundPlayer`.
- [x] Run `./gradlew --no-daemon :domain:test :app:testDebugUnitTest :app:compileDebugKotlin`; all passed.

### Task 3: Split Kotlin/JVM API and implementation modules

**Files:**
- Create: `domain-api/build.gradle.kts`
- Create: `domain-impl/build.gradle.kts`
- Modify: `settings.gradle.kts`
- Modify: `gradle/libs.versions.toml` only if the module plugin aliases require it
- Move public app-facing contracts/models from `domain/src/main/kotlin/com/example/batteryalarm/domain/` to `domain-api/src/main/kotlin/com/example/batteryalarm/domain/`
- Move concrete code and implementation tests from `domain/` to `domain-impl/`
- Modify: `app/build.gradle.kts`

- [x] Create `:domain-api` and `:domain-impl` as Kotlin/JVM modules with toolchain 17; export coroutines as API and move `PrivacyConsentTest` with its model.
- [x] Move implementation code/tests to `:domain-impl`; preserve packages, keep implementations internal, and expose only the factory for `:domain-di` construction.
- [x] Move app-consumed alarm, privacy, and update ports/results into `:domain-api`; factory signatures use API types only.
- [x] Run both domain test tasks and app unit tests; the larger verification also passed `:domain-api:test :domain-impl:test :domain-di:compileDebugKotlin :app:compileDebugKotlin :app:testDebugUnitTest :app:assembleDebug`.

### Task 4: Move domain Hilt bindings out of app and remove implementation compile access

**Files:**
- Create: `domain-di/build.gradle.kts`
- Create: `domain-di/src/main/kotlin/com/example/batteryalarm/di/DomainBindingsModule.kt`
- Move: `app/src/main/java/com/example/batteryalarm/di/ApplicationScope.kt` to `domain-di`
- Create: `domain-di/src/main/kotlin/com/example/batteryalarm/di/RawAlarmController.kt`
- Modify: `app/src/main/java/com/example/batteryalarm/di/AlarmModule.kt`
- Modify: `app/src/main/java/com/example/batteryalarm/di/UpdateModule.kt`
- Modify: `app/src/main/java/com/example/batteryalarm/alarm/AndroidAlarmSoundPlayer.kt`
- Modify: `app/build.gradle.kts`

- [x] Configure `:domain-di` as an Android library using AGP 9 built-in Kotlin and existing Hilt/KSP/metadata versions; no Kotlin Android plugin added.
- [x] Move `ApplicationScope` and add `RawAlarmController` in `:domain-di`; provide raw controller, coordinator, sound session, and update checker.
- [x] Keep Android adapters and analytics in `:app`; its controller decorates the `@RawAlarmController` binding.
- [x] App depends directly on `:domain-api` and `:domain-di`; the DI module depends on `:domain-impl` with `implementation`.
- [x] App Hilt compilation and debug assembly passed, validating aggregation and duplicate-binding checks.

### Task 5: Add the dependency-boundary guard and update project docs/CI

**Files:**
- Create: `.github/scripts/verify-domain-boundary.sh`
- Modify: `.github/workflows/android-ci.yml`
- Modify: `README.md`
- Modify: `NEXT_STEPS.md`
- Modify: domain module/source/test paths as needed for final import cleanup

- [x] Add the boundary script; it checks for API/DI and rejects implementation/legacy domain on the app compile classpath.
- [x] Add the boundary script and new test/build tasks to Android CI.
- [x] Update README architecture, code references, commands and boundary limits; mark the module boundary implemented in NEXT_STEPS.
- [x] Search production and test sources; app contains no imports of `AlarmDomainFactory`, `CheckForAppUpdate`, `Default*`, or concrete volume-ramp classes.

### Task 6: Fresh review, full verification, and one commit on master

**Files:** all implementation files above; no additional scope.

- [ ] Review the complete diff against the design and Review Focus, using a fresh-context reviewer; resolve Critical/Important findings before running final tests.
- [ ] Run `bash .github/scripts/verify-domain-boundary.sh` and inspect both compile/runtime Gradle dependency reports.
- [ ] Run all JVM/unit tests and debug assembly: `./gradlew --no-daemon :domain-api:test :domain-impl:test :app:testDebugUnitTest :app:assembleDebug`.
- [ ] Run full E2E on the already configured API 35 Google APIs emulator: `FULL_E2E=true bash .github/scripts/run-android-e2e.sh`; report the pre-existing `@Ignore` reboot scenario as skipped.
- [ ] Run `git diff --check`, inspect staged paths, and commit the complete refactor as one commit on local `master` with subject `refactor(architecture): split domain api and hilt wiring`.
