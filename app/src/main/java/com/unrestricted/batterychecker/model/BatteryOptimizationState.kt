package com.unrestricted.batterychecker.model

import androidx.compose.ui.graphics.Color

/**
 * Represents the three core Android battery optimization states introduced in modern Android,
 * along with an UNKNOWN state for non-privileged fallback.
 */
enum class BatteryOptimizationState(
    val displayName: String,
    val description: String,
    val badgeColor: Long,
    val badgeContentColor: Long
) {
    UNRESTRICTED(
        displayName = "Unrestricted",
        description = "Runs in background with no restrictions. Uses more battery.",
        badgeColor = 0xFFD1E4FF,
        badgeContentColor = 0xFF00315B
    ),
    OPTIMIZED(
        displayName = "Optimized",
        description = "Balanced by system using dynamic App Standby buckets.",
        badgeColor = 0xFFD8E2FF,
        badgeContentColor = 0xFF003062
    ),
    RESTRICTED(
        displayName = "Restricted",
        description = "Background execution blocked. Notifications may be delayed.",
        badgeColor = 0xFFFFD8E4,
        badgeContentColor = 0xFF3D0023
    ),
    UNKNOWN(
        displayName = "Not Whitelisted",
        description = "Default state. Connect Shizuku or Shevery to reveal if Optimized or Restricted.",
        badgeColor = 0xFFE2E2E6,
        badgeContentColor = 0xFF44474E
    )
}
