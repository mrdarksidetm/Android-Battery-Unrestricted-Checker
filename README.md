# Battery Mode Checker

<p align="center">
  <img src="assets/images/ic_launcher_foreground.svg" width="96" height="96" alt="Battery Mode Checker App Icon" />
</p>

<p align="center">
  <a href="https://github.com/mrdarksidetm/Android-Battery-Unrestricted-Checker/actions/workflows/build.yml"><img src="https://github.com/mrdarksidetm/Android-Battery-Unrestricted-Checker/actions/workflows/build.yml/badge.svg" alt="CI Status" /></a>
  <a href="https://m3.material.io/"><img src="https://img.shields.io/badge/Design-Material_3_Expressive-0061A4?style=flat-square" alt="Material 3 Expressive" /></a>
  <a href="https://developer.android.com/jetpack/compose"><img src="https://img.shields.io/badge/UI-Jetpack_Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose" /></a>
  <a href="https://github.com/RikkaApps/Shizuku"><img src="https://img.shields.io/badge/Bridge-Shizuku_%26_Shevery-7C4DFF?style=flat-square" alt="Shizuku & Shevery" /></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-Apache_2.0-blue.svg?style=flat-square" alt="License" /></a>
</p>

<p align="center">
  A focused, ultra-responsive native Android utility built with <b>Jetpack Compose</b> and <b>Material 3 Expressive</b> to audit, manage, and toggle the true background execution state of every application on your device.
</p>

---

## 📱 Direct Standalone Download

Download the pure production-signed `.apk` directly to your phone without extracting zip files:

<p align="center">
  <a href="https://github.com/mrdarksidetm/Android-Battery-Unrestricted-Checker/releases/latest" target="_blank">
    <img src="assets/images/Direct%20Link%20Frame%20Badge.svg" height="96" alt="Direct APK Download" />
  </a>
</p>

<p align="center">
  <a href="http://apps.obtainium.imranr.dev/redirect.html?r=obtainium://app/%7B%22id%22%3A%22com.unrestricted.batterychecker%22%2C%22url%22%3A%22https%3A%2F%2Fgithub.com%2Fmrdarksidetm%2FAndroid-Battery-Unrestricted-Checker%22%2C%22author%22%3A%22mrdarksidetm%22%2C%22name%22%3A%22Battery%20Mode%20Checker%22%2C%22preferredApkIndex%22%3A0%7D" target="_blank">
    <img src="assets/images/obtanium-badge.png" height="84" alt="Obtainium Badge" />
  </a>
  &nbsp;&nbsp;
  <a href="https://github.com/mrdarksidetm/Android-Battery-Unrestricted-Checker/releases/latest" target="_blank">
    <img src="assets/images/github-badge.png" height="84" alt="GitHub Releases Badge" />
  </a>
</p>

> [!NOTE]
> **Production Signing Keystore**:
> All release builds are automatically signed with a persistent 2048-bit RSA production keystore preserved across builds in GitHub Actions CI. Verified with `apksigner` using APK Signature Schemes v1 & v2.

---

## ✨ Core Features

* ⚡ **Instant Tri-State Auditing**: Accurately classifies all installed packages into **Unrestricted** (Doze exempt), **Optimized** (Dynamic standby bucketing), or **Restricted** (`STANDBY_BUCKET_RESTRICTED`).
* 🎛️ **Live Mode Switching**: Directly toggle any application between Unrestricted, Optimized, and Restricted with one tap via [Shizuku](https://github.com/RikkaApps/Shizuku) and [Shevery](https://github.com/HmnDev-Tech/shevery) privileged binder IPC.
* 🛡️ **Strict Shizuku Guard Lock**: If Shizuku or Shevery is offline or lacks authorization, all app battery data, search controls, and distribution graphs are locked and hidden behind an expressive locked state container with direct authorization actions.
* 📊 **Native Canvas Capsule**: Smooth real-time multi-segment distribution bar rendered directly with Compose `Canvas` and physics-based spring animations.
* 🔍 **Ergonomic Top-Down Search**: Borderless 28dp pill search bar positioned directly above category filter chips for seamless thumb navigation.
* 🏷️ **Expressive Filter Chips**: Fully rounded pill chips (`CircleShape`) with category-specific tonal containers and discrete count badges.
* 🛡️ **Official Shizuku & Framework Vector Logos**:
  - Authentic Shizuku Arcticons 48x48 vector line art (`ic_shizuku_logo.xml`).
  - Dynamic status banner dynamically transitioning from primary accent to emerald green when connected.
  - Official Android (`ic_android_logo.xml`) and Jetpack Compose (`ic_jetpack_compose_logo.xml`) vector logos in `AboutScreen.kt`.
* 📱 **Async Package App Icons**: Real application icons fetched dynamically via `PackageManager` with graceful placeholders inside squircle frames.
* 🧭 **3-Dots Overflow Menu**: Quick access to Refresh, in-app Guide dialog, Show system apps toggle (hidden by default), and About page.
* 🎭 **Android 13+ Themed Icons**: Native Adaptive Icon bundled with a dedicated `<monochrome>` layer for dynamic Material You wallpaper theming.
* 🛡️ **Zero Telemetry & Offline**: 100% offline, zero internet permissions, zero analytics, and zero background services.

---

## 🛠️ Architecture & Specifications

| Component | Technology | Detail |
| :--- | :--- | :--- |
| **Platform** | Native Android | 100% Kotlin & Jetpack Compose (BOM 2024.10.01) |
| **Design Language** | **Material 3 Expressive** | Full surface container token layering, 28dp capsules |
| **Target / Min SDK** | Android 15 (API 35) / Android 8.0 (API 26) | Edge-to-edge transparent system bars |
| **Privilege Engine** | Shizuku API 13.1.5 | Reflection-based binder IPC (compatible with Shizuku & Shevery) |
| **State Management** | Kotlin Coroutines & `StateFlow` | Lifecycle-aware collection with `collectAsStateWithLifecycle` |
| **Binary Output** | Universal FAT APK | Multi-ABI support (`arm64-v8a`, `armeabi-v7a`, `x86_64`, `x86`) |
| **CI / CD** | GitHub Actions | Automated keystore persistence, static analysis, and releases |

---

## 📖 Background: Why This App Exists

In modern Android, battery optimization settings are buried deep within each individual application's system settings submenu. To inspect 150 installed apps, a user would need to navigate through 150 separate screens.

**Battery Mode Checker** consolidates everything into a single, unified dashboard:
1. **Unrestricted**: Exempt from Doze mode and background network/execution caps. Essential for VPNs, music streaming, VoIP, and navigation.
2. **Optimized**: Default dynamic App Standby Bucketing. Android throttles background work based on frequency of use.
3. **Restricted**: App is forcefully placed into `STANDBY_BUCKET_RESTRICTED`. Background tasks, alarms, and network access are aggressively restricted.

---

## 👤 Developer & Philosophy

Built with ❤️ by **Abhijeet Yadav** ([@mrdarksidetm](https://github.com/mrdarksidetm)).

This application adheres to the **Single-Purpose Utility Philosophy**: it is engineered to do one specific job with uncompromising precision, maximum performance, and zero bloat. Because its scope is tightly bounded, it requires no perpetual updates or tracking.

---

## 📄 License

This project is licensed under the [Apache License 2.0](LICENSE).
