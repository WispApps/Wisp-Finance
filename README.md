# Wisp Finance

Wisp Finance is an Android personal finance app built with Kotlin and Jetpack
Compose. It includes expense tracking, categories, budgets, recurring payments,
savings goals, loans, debts, investments, crypto records and finance calculators.

## Languages and setup

English, Russian, Ukrainian, German, Spanish, French, Italian, Dutch, Polish and
Portuguese are supported. Users explicitly select their language and primary
currency; currency is not inferred from the language or region.

The initial setup covers language, recovery phrase, currency and notifications.
Recovery phrases are always displayed in English. Settings includes a preview
of the setup screens using invalid demonstration words without changing real data.

## Backups, recovery and Nextcloud

Portable backups and Nextcloud sync payloads are encrypted using the recovery
key. Nextcloud is optional and uses HTTPS WebDAV with credentials supplied by
the user. To recover an encrypted backup, keep both its file and the matching
recovery phrase. The phrase alone does not contain financial records.

The phrase uses Wisp Finance's own format: 24 hyphenated English word pairs.
It is **not a BIP39 wallet seed**. Keep it private and separate from backup files.
If you copy it, remove it from the clipboard and keyboard history after saving
it securely. Do not include recovery phrases or credentials in bug reports.

The working database on the phone is stored in Android's private app storage;
it is not encrypted with the recovery phrase. The device copy of the recovery
key is wrapped using Android Keystore. Clearing app data or uninstalling removes
the local database and device-held key.

## Build

Requirements are declared in `app/build.gradle.kts`,
`gradle/libs.versions.toml` and `gradle/gradle-daemon-jvm.properties`.
The project currently uses minSdk 26, compile/target SDK 37 and a Java 25 Gradle
toolchain. Network access is needed to resolve the wrapper and dependencies.

Open the project in Android Studio and install the requested SDK/toolchain.
Build a debug APK on Windows:

```powershell
.\gradlew.bat :app:assembleDebug
```

Or on Linux/macOS:

```sh
./gradlew :app:assembleDebug
```

An unsigned release is built with `:app:assembleRelease`. R8 and resource
shrinking are enabled for the release build.

On Windows, `tools/build-and-sign-wisp-finance-release.ps1` builds and signs a
release using the existing key at `$HOME\WispApps-signing\wisp-finance-release.p12`
and alias `wisp-finance`. The helper requires Android SDK Build Tools 34.0.0 and
asks for the keystore password directly through apksigner. Private keys and
passwords must remain outside Git.

## Testing

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:lintRelease
```

Instrumented tests are under `app/src/androidTest`. Run
`:app:connectedDebugAndroidTest` on a disposable emulator without personal data.
Debug and release signatures differ; do not uninstall a real user's release
installation merely to run instrumentation tests.

## F-Droid and releases

The project metadata is stored in `fdroid/metadata/com.wisp.finance.yml`.
F-Droid submission is maintained separately from this app's source repository.
Metadata does not mean the app has already been accepted or that reproducible
build verification has passed.

Before publishing, ensure the version, source tag, signing-certificate digest
and public APK release URL agree. Publish an APK built from the tagged source,
then validate the F-Droid build and its reproducibility. Keep a verified backup
of the release signing key and its password separately.

The initial version is `1.0` (versionCode `1`); its planned tag is `v1.0`.
Internal patch labels such as Build 26 are not Android version codes.

## License

Wisp Finance is licensed under the GNU General Public License, version 3 or
(at your option) any later version (`GPL-3.0-or-later`). See [LICENSE](LICENSE).
Third-party dependencies remain under their respective licenses.
