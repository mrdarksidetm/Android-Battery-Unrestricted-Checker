package com.unrestricted.batterychecker.service

import android.content.Context
import android.content.pm.PackageManager
import android.os.PowerManager
import android.util.Log
import com.unrestricted.batterychecker.model.BatteryOptimizationState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

object ShizukuBatteryBridge {
    private const val TAG = "ShizukuBatteryBridge"

    /**
     * Checks if Shizuku server is alive and responding.
     */
    fun isShizukuAvailable(): Boolean {
        return try {
            Shizuku.pingBinder()
        } catch (e: Throwable) {
            false
        }
    }

    /**
     * Checks if this app has permission to invoke Shizuku commands.
     */
    fun hasPermission(): Boolean {
        return try {
            if (!isShizukuAvailable()) return false
            if (Shizuku.isPreV11()) return false
            Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (e: Throwable) {
            false
        }
    }

    /**
     * Requests Shizuku permission.
     */
    fun requestPermission(requestCode: Int = 1001) {
        try {
            if (isShizukuAvailable() && !Shizuku.isPreV11()) {
                Shizuku.requestPermission(requestCode)
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to request Shizuku permission", e)
        }
    }

    /**
     * Fetches the complete DeviceIdle (Doze) whitelist by querying dumpsys via Shizuku.
     */
    suspend fun getShizukuPowerWhitelist(): Set<String> = withContext(Dispatchers.IO) {
        val whitelisted = mutableSetOf<String>()
        if (!hasPermission()) return@withContext whitelisted

        try {
            val process = Shizuku.newProcess(arrayOf("dumpsys", "deviceidle", "whitelist"), null, null)
            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                reader.lineSequence().forEach { line ->
                    val trimmed = line.trim()
                    if (trimmed.contains(",")) {
                        val parts = trimmed.split(",")
                        if (parts.isNotEmpty()) {
                            whitelisted.add(parts[0].trim())
                        }
                    } else if (trimmed.startsWith("system:") || trimmed.startsWith("user:")) {
                        val pkg = trimmed.substringAfter(":").trim()
                        if (pkg.isNotEmpty()) {
                            whitelisted.add(pkg)
                        }
                    }
                }
            }
            process.waitFor()
        } catch (e: Throwable) {
            Log.e(TAG, "Error fetching Shizuku power whitelist", e)
        }
        whitelisted
    }

    /**
     * Queries all packages having RUN_ANY_IN_BACKGROUND ignored (Restricted).
     */
    suspend fun getRestrictedPackages(): Set<String> = withContext(Dispatchers.IO) {
        val restricted = mutableSetOf<String>()
        if (!hasPermission()) return@withContext restricted

        try {
            // Check AppOps query-op for RUN_ANY_IN_BACKGROUND with ignore/deny
            val process = Shizuku.newProcess(
                arrayOf("cmd", "appops", "query-op", "--user", "0", "RUN_ANY_IN_BACKGROUND", "ignore"),
                null,
                null
            )
            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                reader.lineSequence().forEach { line ->
                    val trimmed = line.trim()
                    // Lines typically look like "com.example.app: ..." or package name
                    val pkg = trimmed.substringBefore(":").substringBefore(" ").trim()
                    if (pkg.isNotEmpty() && pkg.contains(".")) {
                        restricted.add(pkg)
                    }
                }
            }
            process.waitFor()
        } catch (e: Throwable) {
            Log.e(TAG, "Error fetching restricted appops", e)
        }
        restricted
    }

    /**
     * Evaluates the battery state for a package given whitelist and restricted sets.
     */
    fun evaluateState(
        packageName: String,
        isWhitelisted: Boolean,
        isRestricted: Boolean,
        shizukuActive: Boolean
    ): BatteryOptimizationState {
        return if (shizukuActive) {
            when {
                isWhitelisted -> BatteryOptimizationState.UNRESTRICTED
                isRestricted -> BatteryOptimizationState.RESTRICTED
                else -> BatteryOptimizationState.OPTIMIZED
            }
        } else {
            // Non-Shizuku fallback: standard public API
            if (isWhitelisted) {
                BatteryOptimizationState.UNRESTRICTED
            } else {
                BatteryOptimizationState.UNKNOWN
            }
        }
    }

    /**
     * Changes an app's optimization mode directly using Shizuku.
     */
    suspend fun setOptimizationMode(
        packageName: String,
        targetState: BatteryOptimizationState
    ): Boolean = withContext(Dispatchers.IO) {
        if (!hasPermission()) return@withContext false

        try {
            when (targetState) {
                BatteryOptimizationState.UNRESTRICTED -> {
                    // Add to whitelist and allow background execution
                    val p1 = Shizuku.newProcess(arrayOf("dumpsys", "deviceidle", "whitelist", "+$packageName"), null, null)
                    p1.waitFor()
                    val p2 = Shizuku.newProcess(arrayOf("cmd", "appops", "set", packageName, "RUN_ANY_IN_BACKGROUND", "allow"), null, null)
                    p2.waitFor()
                    true
                }
                BatteryOptimizationState.OPTIMIZED -> {
                    // Remove from whitelist and allow background execution (standard adaptive)
                    val p1 = Shizuku.newProcess(arrayOf("dumpsys", "deviceidle", "whitelist", "-$packageName"), null, null)
                    p1.waitFor()
                    val p2 = Shizuku.newProcess(arrayOf("cmd", "appops", "set", packageName, "RUN_ANY_IN_BACKGROUND", "allow"), null, null)
                    p2.waitFor()
                    true
                }
                BatteryOptimizationState.RESTRICTED -> {
                    // Remove from whitelist and ignore/deny background execution
                    val p1 = Shizuku.newProcess(arrayOf("dumpsys", "deviceidle", "whitelist", "-$packageName"), null, null)
                    p1.waitFor()
                    val p2 = Shizuku.newProcess(arrayOf("cmd", "appops", "set", packageName, "RUN_ANY_IN_BACKGROUND", "ignore"), null, null)
                    p2.waitFor()
                    true
                }
                BatteryOptimizationState.UNKNOWN -> false
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to set optimization mode for $packageName", e)
            false
        }
    }

    /**
     * Fallback method using standard PowerManager.
     */
    fun checkPublicWhitelist(context: Context, packageName: String): Boolean {
        return try {
            val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            pm?.isIgnoringBatteryOptimizations(packageName) == true
        } catch (e: Throwable) {
            false
        }
    }
}
