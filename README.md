# iFlow

iFlow is a local-first Android bookkeeping app built for fast personal expense tracking.

The app focuses on a quiet daily workflow:

- Manual income and expense entry.
- Local Room database storage.
- Transaction list with edit and soft delete.
- Notification-listener framework for payment notification parsing.
- Pending queue for automatically parsed transactions.
- Monthly income, expense, balance, and category ranking.
- Local JSON and CSV export.

## Privacy

iFlow is designed as a private local ledger. Data is stored on the device by default. The app does not upload ledger data, does not use a cloud backend, and does not include third-party analytics SDKs.

Notification content is used only for local payment parsing when notification access is enabled by the user. Manual bookkeeping works without notification access.

## Tech Stack

- Kotlin
- Jetpack Compose
- Room
- Coroutines and Flow
- Single Activity architecture
- NotificationListenerService

## Project Layout

- `src/` contains the Android project, source code, tests, and Gradle files.
- `plan/` and `report/` are local working documents and are intentionally not tracked in the public repository.

## Build

The Gradle Wrapper currently uses a local Gradle distribution zip path because the development machine had unreliable access to the Gradle distribution service:

```text
E:/iflow/gradle-8.7-bin.zip
```

With JDK 17 and Android SDK 35 available:

```powershell
cd src
.\gradlew.bat testDebugUnitTest assembleDebug
```

The debug APK is generated at:

```text
src/app/build/outputs/apk/debug/app-debug.apk
```
