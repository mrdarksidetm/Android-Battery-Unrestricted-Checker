package com.unrestricted.batterychecker.viewmodel

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.unrestricted.batterychecker.model.AppBatteryInfo
import com.unrestricted.batterychecker.model.BatteryOptimizationState
import com.unrestricted.batterychecker.model.FilterMode
import com.unrestricted.batterychecker.model.OptimizationStats
import com.unrestricted.batterychecker.service.ShizukuBatteryBridge
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku

class BatteryCheckerViewModel(application: Application) : AndroidViewModel(application) {

    private val _rawApps = MutableStateFlow<List<AppBatteryInfo>>(emptyList())
    val rawApps: StateFlow<List<AppBatteryInfo>> = _rawApps.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(FilterMode.ALL)
    val selectedFilter: StateFlow<FilterMode> = _selectedFilter.asStateFlow()

    // Default to false: system apps are hidden unless user explicitly ticks "Show system apps"
    private val _showSystemApps = MutableStateFlow(false)
    val showSystemApps: StateFlow<Boolean> = _showSystemApps.asStateFlow()

    private val _isShizukuConnected = MutableStateFlow(false)
    val isShizukuConnected: StateFlow<Boolean> = _isShizukuConnected.asStateFlow()

    private val _isShizukuPermissionGranted = MutableStateFlow(false)
    val isShizukuPermissionGranted: StateFlow<Boolean> = _isShizukuPermissionGranted.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Filtered list based on search, category filter, and system apps toggle
    val filteredApps: StateFlow<List<AppBatteryInfo>> = combine(
        _rawApps,
        _searchQuery,
        _selectedFilter,
        _showSystemApps
    ) { apps, query, filter, showSystem ->
        apps.filter { app ->
            if (!showSystem && app.isSystemApp) {
                return@filter false
            }

            val matchesQuery = query.isBlank() ||
                    app.appName.contains(query, ignoreCase = true) ||
                    app.packageName.contains(query, ignoreCase = true)

            val matchesFilter = when (filter) {
                FilterMode.ALL -> true
                FilterMode.UNRESTRICTED -> app.state == BatteryOptimizationState.UNRESTRICTED
                FilterMode.OPTIMIZED -> app.state == BatteryOptimizationState.OPTIMIZED
                FilterMode.RESTRICTED -> app.state == BatteryOptimizationState.RESTRICTED
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Real-time statistics respecting system app visibility
    val stats: StateFlow<OptimizationStats> = combine(_rawApps, _showSystemApps) { apps, showSystem ->
        val visibleApps = if (showSystem) apps else apps.filter { !it.isSystemApp }
        OptimizationStats(
            totalApps = visibleApps.size,
            unrestrictedCount = visibleApps.count { it.state == BatteryOptimizationState.UNRESTRICTED },
            optimizedCount = visibleApps.count { it.state == BatteryOptimizationState.OPTIMIZED },
            restrictedCount = visibleApps.count { it.state == BatteryOptimizationState.RESTRICTED },
            unknownCount = visibleApps.count { it.state == BatteryOptimizationState.UNKNOWN }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OptimizationStats())

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        checkShizukuState()
        loadApps()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        checkShizukuState()
        loadApps()
    }

    init {
        try {
            Shizuku.addBinderReceivedListenerSticky(binderReceivedListener)
            Shizuku.addBinderDeadListener(binderDeadListener)
        } catch (e: Throwable) {
            // Ignored if Shizuku binder registration fails
        }
        checkShizukuState()
        loadApps()
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onFilterSelected(filter: FilterMode) {
        _selectedFilter.value = filter
    }

    fun toggleShowSystemApps() {
        _showSystemApps.value = !_showSystemApps.value
    }

    fun checkShizukuState() {
        _isShizukuConnected.value = ShizukuBatteryBridge.isShizukuAvailable()
        _isShizukuPermissionGranted.value = ShizukuBatteryBridge.hasPermission()
    }

    fun requestShizukuPermission() {
        ShizukuBatteryBridge.requestPermission()
    }

    fun loadApps() {
        viewModelScope.launch {
            _isLoading.value = true
            val context = getApplication<Application>()
            val pm = context.packageManager

            val appList = withContext(Dispatchers.IO) {
                checkShizukuState()
                val shizukuReady = _isShizukuPermissionGranted.value

                val shizukuWhitelist = if (shizukuReady) {
                    ShizukuBatteryBridge.getShizukuPowerWhitelist()
                } else emptySet()

                val restrictedSet = if (shizukuReady) {
                    ShizukuBatteryBridge.getRestrictedPackages()
                } else emptySet()

                val installedPackages = pm.getInstalledApplications(PackageManager.GET_META_DATA)

                installedPackages.map { appInfo ->
                    val pkg = appInfo.packageName
                    val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

                    val isWhitelisted = if (shizukuReady) {
                        shizukuWhitelist.contains(pkg)
                    } else {
                        ShizukuBatteryBridge.checkPublicWhitelist(context, pkg)
                    }

                    val isRestricted = if (shizukuReady) {
                        restrictedSet.contains(pkg)
                    } else false

                    val state = ShizukuBatteryBridge.evaluateState(
                        packageName = pkg,
                        isWhitelisted = isWhitelisted,
                        isRestricted = isRestricted,
                        shizukuActive = shizukuReady
                    )

                    val appName = try {
                        pm.getApplicationLabel(appInfo).toString()
                    } catch (e: Exception) {
                        pkg
                    }

                    val versionName = try {
                        pm.getPackageInfo(pkg, 0).versionName ?: "1.0"
                    } catch (e: Exception) {
                        "1.0"
                    }

                    AppBatteryInfo(
                        packageName = pkg,
                        appName = appName,
                        versionName = versionName,
                        isSystemApp = isSystem,
                        state = state,
                        uid = appInfo.uid,
                        icon = null
                    )
                }.sortedBy { it.appName.lowercase() }
            }

            _rawApps.value = appList
            _isLoading.value = false
        }
    }

    fun setAppOptimizationMode(app: AppBatteryInfo, targetState: BatteryOptimizationState) {
        viewModelScope.launch {
            val success = ShizukuBatteryBridge.setOptimizationMode(app.packageName, targetState)
            if (success) {
                _rawApps.value = _rawApps.value.map {
                    if (it.packageName == app.packageName) {
                        it.copy(state = targetState)
                    } else it
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            Shizuku.removeBinderReceivedListener(binderReceivedListener)
            Shizuku.removeBinderDeadListener(binderDeadListener)
        } catch (e: Throwable) {
            // Safe cleanup
        }
    }
}
