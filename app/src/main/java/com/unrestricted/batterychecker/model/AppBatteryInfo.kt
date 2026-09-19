package com.unrestricted.batterychecker.model

import android.graphics.drawable.Drawable

data class AppBatteryInfo(
    val packageName: String,
    val appName: String,
    val versionName: String,
    val isSystemApp: Boolean,
    val state: BatteryOptimizationState,
    val uid: Int,
    val appStandbyBucket: String? = null,
    val icon: Drawable? = null
)

data class OptimizationStats(
    val totalApps: Int = 0,
    val unrestrictedCount: Int = 0,
    val optimizedCount: Int = 0,
    val restrictedCount: Int = 0,
    val unknownCount: Int = 0
)

enum class FilterMode(val label: String) {
    ALL("All"),
    UNRESTRICTED("Unrestricted"),
    OPTIMIZED("Optimized"),
    RESTRICTED("Restricted"),
    USER_ONLY("Third-Party"),
    SYSTEM_ONLY("System")
}
