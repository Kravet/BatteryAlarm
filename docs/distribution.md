# APK Distribution

Battery Alarm APKs are distributed from the public [`Kravet/battery-alarm-releases`](https://github.com/Kravet/battery-alarm-releases) repository. This repository is separate from the Android source repository, so public downloads can continue if the source repository becomes private. It contains release information only; APK files belong on GitHub Release pages and are not committed to Git.

## Release channels and naming

- **Developer beta:** early builds for Android developers. Use version tags such as `v0.1.0-beta.1` and `v0.1.0-beta.2`, and mark each GitHub Release as a prerelease.
- **Public beta:** builds tested with friends, family, and communities. Keep these marked as prereleases until a version is ready for general use.
- **Stable:** publish a normal GitHub Release with tags such as `v1.0.0`, `v1.1.0`, and `v1.2.0`.
- **Stable APK asset:** name the APK `BatteryAlarm.apk`. Do not commit APKs to either repository's Git history. Keep each release's tag, release notes, and asset version aligned.

GitHub's `/releases/latest` endpoint selects the latest non-prerelease release. A future stable Update Checker can use that endpoint. A beta Update Checker must instead query the Releases API and select a prerelease according to the beta channel; it must not assume `/releases/latest` includes beta releases.

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

Do not create a release until a real, signed APK is ready. No APK release is currently published by this setup task.

## Compatibility and signing

The source currently declares application ID `com.kravets.batteryalarm`, `versionCode` `1`, and `versionName` `1.0.0`. Every update APK must use the same application ID and a version code greater than the installed version. Android also requires an update to be signed by the same signing key, or a valid signing-key lineage. Preserve the production signing key securely; never commit or document key material or passwords. Verify the signing certificate of every release build before upload.

Before moving distribution to Google Play, confirm the Play app's package name and signing certificate strategy preserve update compatibility for people who installed a sideloaded APK. If Play App Signing changes which certificate signs delivered APKs, plan the transition before publishing there.

## Future Update Checker

The Update Checker is not implemented. When planned separately, it should use this public repository as its release source, compare the installed version with the correct stable or beta channel, and direct users to the corresponding GitHub release. Stable checks can use the latest stable release URL above; beta checks need prerelease-aware release lookup. Do not present automatic updates as available until that feature exists.
