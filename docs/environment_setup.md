# Environment Setup (Android Java)

## Toolchain
- Android Studio: latest stable
- JDK: 17
- AGP: 8.13.2
- Gradle Wrapper: 8.13

## Android SDK
- compileSdk: 35
- targetSdk: 35
- minSdk: 29

## Build steps
1. Open this folder in Android Studio.
2. Let Android Studio sync Gradle dependencies.
3. Build APK from Build > Build Bundle(s) / APK(s) > Build APK(s).
4. Install generated APK on device (offline mode is supported).

## Command-line build (optional)
- This machine has no system Java runtime on PATH.
- Use Android Studio bundled JBR as JAVA_HOME:
  `export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"`
- Then run:
  `./gradlew testDebugUnitTest`
  `./gradlew assembleDebug`

## Data mode
- Runtime: local Room DB only (offline)
- Content management: MySQL + Navicat (export data then seed into APK)
