# 📱 AppTrack

> **Track all your apps in one place.** A modern, premium Android client dashboard for monitoring live store availability, release statuses, and app health.

---

## ✨ Features

- 📊 **Real-time App Monitoring**: Track live app status across Play Store and external stores.
- 🎨 **Modern Material 3 UI**: Built natively with Jetpack Compose featuring sleek animations, dark mode, and responsive layouts.
- 🔄 **Offline-First & Cloud Sync**: Local persistence powered by Room Database with seamless Firebase Firestore synchronization.
- 🔐 **Authentication & Security**: Secure client authentication via Firebase Auth and environment secret management.
- 🚀 **Automated CI/CD**: Continuous integration and release pipeline configured with GitHub Actions.

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **Architecture**: Clean Architecture (MVVM + StateFlow / Coroutines)
- **Local Storage**: Room Database
- **Backend & Sync**: Firebase Firestore & Firebase Auth
- **Networking**: Retrofit2 + OkHttp3
- **Build System**: Gradle Kotlin DSL (`build.gradle.kts`)

---

## 🚀 Getting Started

### Prerequisites

- Android Studio Ladybug (or newer)
- JDK 17+
- Android SDK 35/36

### Setup & Run

1. **Clone the repository:**
   ```bash
   git clone https://github.com/mzaid-dev/AppTrack.git
   cd AppTrack
   ```

2. **Setup environment variables:**
   ```bash
   cp .env.example .env
   ```

3. **Build and run:**
   ```bash
   ./gradlew assembleDebug
   ```

---

## 📦 Releases & CI/CD

Automated builds and APK release artifacts are generated on push to `main` via [GitHub Actions](.github/workflows/release.yml).
