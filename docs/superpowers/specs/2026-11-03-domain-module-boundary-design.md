# Domain Module Boundary Design

## Intent

Make the production compile graph enforce that `:app` consumes domain contracts and Hilt composition without compiling against concrete domain implementations. This is an interview-ready example of a real Gradle boundary, not a behavior change.

## Goals

- `:app` directly depends on `:domain-api` and `:domain-di`; it has no compile dependency on `:domain-impl`.
- `:domain-api` stays free of Android and Hilt dependencies and contains the contracts/models used across the app boundary.
- `:domain-impl` stays Kotlin/JVM and owns concrete domain logic, its unit tests, and the public construction factory used only by `:domain-di`.
- `:domain-di` owns Hilt bindings and shared DI qualifiers. It depends on API and implementation, and never depends on `:app`.
- Android adapters, Firebase analytics, app configuration, and URL policy implementation remain in `:app`.
- Preserve runtime behavior, singleton lifetimes, update validation, and the analytics decorator.

## Non-goals

- This does not remove implementation classes from the APK/runtime graph or provide JVM/reflection/security isolation. It enforces the app source compile boundary through Gradle dependency metadata.
- Do not redesign alarm/update behavior, move Android adapters, add a dynamic feature, or split unrelated features into more modules.
- Do not change Kotlin, AGP, Hilt, KSP, or coroutine versions as part of this refactor.

## Approaches considered

1. **Keep `:app -> :domain` and the public factory.** Lowest cost, but API and implementation remain one artifact and app source still invokes the implementation factory.
2. **Split API and implementation, keep Hilt in `:app`.** The app's provider code would need implementation construction symbols and an implementation compile dependency, so this does not meet the requested boundary.
3. **Use `:domain-api`, `:domain-impl`, and `:domain-di`. Selected.** The app depends on API and Hilt composition; DI depends on API and implementation; implementation depends on API. This best matches the stated compile-time boundary, at the cost of additional Gradle/Hilt wiring.

## Module responsibilities and wiring

```text
:app -> :domain-api
:app -> :domain-di
:domain-di -> :domain-api
:domain-di -> :domain-impl  (implementation dependency)
:domain-impl -> :domain-api
```

- `:domain-api` owns alarm ports/results/state, privacy contracts/models, update contracts/results, `AppUpdateChecker`, `ReleasePageUrlPolicy`, and the `AlarmSoundSession` interface. Since its public API exposes coroutine types, it exports the coroutine dependency.
- `:domain-impl` owns the default controller/coordinator, concrete sound session and volume ramp logic, update-check implementation, and `AlarmDomainFactory`. Concrete implementations remain Kotlin `internal`; the factory returns API interfaces.
- `:domain-di` owns `ApplicationScope`, `RawAlarmController`, and Hilt providers for the raw controller, coordinator, sound session, and update checker. It consumes app-provided ports and policy through API types.
- `:app` keeps Android adapter providers, `UpdateConfig`, `GitHubReleaseUrl`, `AndroidUpdateOpener`, and `AnalyticsAlarmController`. Its unqualified singleton controller decorates the qualified raw controller; the coordinator receives that unqualified decorated binding. URL opening and update checking share the same `ReleasePageUrlPolicy` binding.

## Compatibility and validation

- Convert `AlarmSoundSession` from a concrete class into an API interface; provide one singleton session from `:domain-di` through the implementation factory. `AndroidAlarmSoundPlayer` receives this API object rather than constructing `VolumeRampController`.
- Make update presentation depend on `AppUpdateChecker`; keep the checker implementation and its direct behavior tests in `:domain-impl`. The app supplies `ReleasePageUrlPolicy` from its existing config/URL validator.
- Move implementation-focused tests beside `:domain-impl`; keep app tests on API fakes.
- Configure `:domain-di` as a normal Android library using the repository's existing AGP 9, Hilt, KSP, and metadata workaround. Hilt aggregation must discover the installed module transitively.
- Verify the debug compile classpath includes API and DI but excludes implementation, then run all JVM/unit tests, app debug assembly, and full non-ignored API 35 instrumentation before committing.

## Risks and costs

The Hilt graph, app-owned URL policy, scope qualifier, sound-session construction, and update checker interface all cross the new boundary. A mistake can cause duplicate bindings, missing Hilt aggregation, or a runtime validation difference. Four modules also add build/processor setup to a small app; the compile-classpath acceptance check and unchanged behavior tests are required evidence that the cost buys the intended boundary.
