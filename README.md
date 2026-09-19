# Android Battery Unrestricted Checker

[![Build & Release Universal APK](https://github.com/mrdarksidetm/Android-Battery-Unrestricted-Checker/actions/workflows/build.yml/badge.svg)](https://github.com/mrdarksidetm/Android-Battery-Unrestricted-Checker/actions/workflows/build.yml)
[![Material 3 Expressive](https://img.shields.io/badge/Design-Material_3_Expressive-1B6EF3?style=for-the-badge)](https://m3.material.io/)
[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://android.com)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)

A dedicated, native Android application built with **Jetpack Compose** and **Material 3 Expressive** that audits and displays the true battery optimization state of every installed app:
* **Unrestricted** (Whitelisted, no background limits)
* **Optimized** (Default dynamic App Standby bucketing)
* **Restricted** (`STANDBY_BUCKET_RESTRICTED`, background execution blocked)

---

## Key Features

- **Full Tri-State Detection**: Uses [Shizuku](https://github.com/RikkaApps/Shizuku) and [Shevery](https://github.com/HmnDev-Tech/shevery) privileged IPC bridges to query `AppOpsManager.OP_RUN_ANY_IN_BACKGROUND` and `IDeviceIdleController` allowlists directly.
- **Graceful Non-Root Fallback**: If Shizuku is not running, seamlessly inspects public battery optimization whitelists while guiding users on how to enable full detection.
- **Native Canvas Visualization**: Displays a real-time, animated multi-segment distribution capsule rendered with native Compose `Canvas`.
- **Direct Battery Mode Switcher**: Change any app's mode between Unrestricted, Optimized, and Restricted directly with one tap using Shizuku.
- **Search & Filter Chips**: Instantly filter apps by mode (*All*, *Unrestricted*, *Optimized*, *Restricted*, *Third-Party*, *System*).
- **One-Tap System Settings**: Direct deep-links to each application's system info page.

---

## Architecture & Technology

- **UI Framework**: Pure Kotlin + Jetpack Compose (100% Native Android).
- **Design System**: Material 3 Expressive (dynamic tonal colors, typography scale, fluid motion).
- **State Management**: Reactive Kotlin `StateFlow` and `collectAsStateWithLifecycle`.
- **Privilege Engine**: `dev.rikka.shizuku:api` (compatible with Shizuku, Sui, and Shevery).
- **Automated CI/CD**: Remote GitHub Actions workflow with Gradle caching, static analysis, and universal APK generation.

---

## Download Universal APK

Download the latest universal `.apk` binary from the [Releases](https://github.com/mrdarksidetm/Android-Battery-Unrestricted-Checker/releases) section or under the **Artifacts** tab of any GitHub Actions build run.

---

## Development & Building

The project uses GitHub Actions for continuous integration. Local builds are not required.

To run remotely:
```bash
git push origin main
```
The automated workflow will compile the code, perform lint analysis, package the universal APK, and publish it automatically.
