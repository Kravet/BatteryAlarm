# APK Distribution

Battery Alarm APKs are distributed from the public [`Kravet/battery-alarm-releases`](https://github.com/Kravet/battery-alarm-releases) repository. This repository is separate from the Android source repository, so public downloads can continue if the source repository becomes private. It contains release information only; APK files belong on GitHub Release pages and are not committed to Git.

## Release channels and naming

- **Developer beta:** early builds for Android developers. Use version tags such as `v0.1.0-beta.1` and `v0.1.0-beta.2`, and mark each GitHub Release as a prerelease.
- **Public beta:** builds tested with friends, family, and communities. Keep these marked as prereleases until a version is ready for general use.
- **Stable:** publish a normal GitHub Release with tags such as `v1.0.0`, `v1.1.0`, and `v1.2.0`.
- **Stable APK asset:** name the APK `BatteryAlarm.apk`. Do not commit APKs to either repository's Git history. Keep each release's tag, release notes, and asset version aligned.

GitHub's `/releases/latest` endpoint selects the latest non-prerelease release. The implemented in-app Update Checker uses that stable-only endpoint. Beta releases are intentionally not discovered; any future beta checker would need prerelease-aware API logic.

## Stable distribution URLs

- Repository: <https://github.com/Kravet/battery-alarm-releases>
- Releases: <https://github.com/Kravet/battery-alarm-releases/releases>
- Latest stable release: <https://github.com/Kravet/battery-alarm-releases/releases/latest>
- Direct stable APK: <https://github.com/Kravet/battery-alarm-releases/releases/latest/download/BatteryAlarm.apk>

The latest-release and direct-APK URLs will resolve to a downloadable release only after the first normal stable release with a `BatteryAlarm.apk` asset is published.

## Manual release procedure

1. Choose and verify the release version and channel. Use a `v`-prefixed tag matching the version, and mark beta releases as prereleases.
2. Build the intended Android **release** variant. Do not use a debug APK.
3. Confirm the APK is signed with the intended production signing key and that its package name and version match the release. Never upload signing keys, passwords, or local signing configuration.
4. Install and smoke-test the exact signed APK on a device before publishing it.
5. Create a GitHub Release in `Kravet/battery-alarm-releases` for the tag. Attach the APK as a release asset named `BatteryAlarm.apk`; include concise notes describing changes and known issues. Mark beta releases as prereleases and stable releases as normal releases.
6. Open the public release page and verify the tag, prerelease status, asset name, and download before sharing the link.

Do not publish a release until its actual signed APK has been built, installed and smoke-tested.

## Compatibility and signing

The source declares application ID `com.kravets.batteryalarm`; read `app/build.gradle.kts` for the current `versionCode` and `versionName`. Every update APK must use the same application ID and a version code greater than the installed version. Android also requires an update to be signed by the same signing key, or a valid signing-key lineage. Preserve the production signing key securely; never commit or document key material or passwords. Verify the signing certificate of every release build before upload.

### Google Play (future) — keep sideload upgrades working

People who installed a GitHub/sideload APK can later update from Google Play only if the certificate on the device matches Play's **app signing key**. Use this strategy when first publishing to Play:

1. Keep one application ID: `com.kravets.batteryalarm`.
2. Sign every public GitHub/sideload release APK with the same production keystore used today (the release `.jks` configured via `keystore.properties` / `BATTERYALARM_*`).
3. On first Play listing, enable Play App Signing and **import that existing production release key as the app signing key**. Do not let Google generate a new app signing key for this package if sideloaded users should be able to upgrade in place.
4. Create a separate **upload key** for signing AABs uploaded to Play Console. Use the upload key only for Play uploads; do not sign public GitHub APKs with it.
5. Keep an offline backup of the production keystore and store passwords outside the repository. Never commit `.jks` files, passwords, or `keystore.properties`.

If Play is enrolled with a Google-generated app signing key instead, devices with the sideloaded APK will not accept Play updates without uninstalling (or a different package name).

## In-app Update Checker (implemented)

The application checks the latest **stable** GitHub release from this public distribution repository, parses SemVer tags, compares the installed version and displays an in-app update action linking to an allowlisted release page. Attempts are throttled to approximately once every 24 hours. The checker does **not** install an APK, provide system push notifications, or discover prereleases. Verify the release on the public repository before announcing an update.
