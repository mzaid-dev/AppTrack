<div align="center">

# 📱 AppTrack

**Production-grade Android client portal for real-time Google Play Store availability tracking, live status monitoring, and cloud-synchronized catalog management.**

[![Platform](https://img.shields.io/badge/Platform-Android_24%2B-10B981?style=flat&logo=android&logoColor=white&labelColor=0F172A)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-8B5CF6?style=flat&logo=kotlin&logoColor=white&labelColor=0F172A)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Compose-Material_3-3B82F6?style=flat&logo=jetpackcompose&logoColor=white&labelColor=0F172A)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Firebase-Auth_%26_Firestore-F59E0B?style=flat&logo=firebase&logoColor=white&labelColor=0F172A)](https://firebase.google.com)
[![Architecture](https://img.shields.io/badge/Architecture-Clean_%2F_MVVM-6366F1?style=flat&logo=googlecloud&logoColor=white&labelColor=0F172A)](https://developer.android.com/topic/architecture)

</div>

---

## ⚡ Highlights

- **Auto-Audit on Open**: Scans Google Play Store endpoints concurrently across all projects at launch.
- **Granular Badge Updates**: Status mutations save directly to SQLite Room; badges update in-place without list rebuilds or scroll jumps.
- **Segmented Tabs**: Independent views and counters for **Apps** and **Games**.
- **Cloud Sync + Offline First**: Server-first Cloud Firestore fetch with instant offline Room caching.
- **Direct Store Launcher**: One-tap intent to view live apps directly on Google Play Store.

---

## 🏷️ Status Badges

| Badge | State | Description |
| :--- | :---: | :--- |
| ![Live on Play Store](https://img.shields.io/badge/Live_on_Play_Store-10B981?style=flat&logo=googleplay&logoColor=white) | `LIVE` | App is publicly indexed and available on Google Play. |
| ![Not on Store](https://img.shields.io/badge/Not_on_Store-E84B3A?style=flat&logo=googleplay&logoColor=white) | `NOT_FOUND` | Store returned 404 or "Item not found" (unreleased / delisted). |
| ![Unable to Check](https://img.shields.io/badge/Unable_to_Check-EF4444?style=flat) | `UNABLE_TO_CHECK` | Network timeout, DNS failure, or connection error. |
| ![Pending Check](https://img.shields.io/badge/Pending_Check-94A3B8?style=flat) | `PENDING` | Initial state awaiting verification. |
| ![Checking](https://img.shields.io/badge/Checking…-3B82F6?style=flat) | *Transient* | Active background coroutine scanning the store. |

---

## 🏗️ Architecture

```mermaid
graph LR
    UI[Jetpack Compose UI] --> VM[MainViewModel]
    VM --> Repo[AppRepository]
    Repo --> Room[(Room SQLite)]
    Repo --> Firestore[(Cloud Firestore)]
    Repo --> Checker[LiveStatusChecker]
    Checker --> PlayStore((Google Play))
```

- **UI**: Jetpack Compose + Material 3 with reactive `StateFlow`.
- **Data**: Single Source of Truth via Room SQLite database.
- **Sync**: Server-first Firestore query scoped to `currentUserEmail`.
- **Verifier**: Parallel OkHttp3 scanner with mobile Android User-Agent.

---

## 🗄️ Firestore Schema

### `apps` Collection
| Field | Type | Description |
| :--- | :--- | :--- |
| `title` | `string` | App display name |
| `packageName` | `string` | Unique package ID (`com.example.app`) |
| `category` | `string` | `"APP"` or `"GAME"` |
| `userEmail` | `string` | Client owner email for access control |
| `status` | `string` | `"LIVE"`, `"NOT_FOUND"`, `"PENDING"`, `"UNABLE_TO_CHECK"` |
| `iconUrl` | `string` *(optional)* | Custom icon URL (falls back to theme glyph) |
| `liveStoreUrl` | `string` *(optional)* | Direct Play Store link |
| `lastCheckedTimestamp` | `number` | Unix timestamp of last verification |

### `users` Collection
`email` (`string`), `name` (`string`), `role` (`string`).

---

## 🚀 Quick Start

### 1. Prerequisites
- Android Studio Ladybug (2024.2.1+) & JDK 17+
- Add `google-services.json` to `app/`

### 2. Build Commands
```bash
# Debug APK
./gradlew assembleDebug

# Release APK
./gradlew assembleRelease
```

---

## 🌿 Git Workflow

- **`develop`**: Active integration branch for all features and fixes.
- **`main`**: Production releases. Pushing to `main` triggers automated CI/CD signed APK builds.

---

<div align="center">

Made with ❤️ using **Jetpack Compose** & **Kotlin**

</div>