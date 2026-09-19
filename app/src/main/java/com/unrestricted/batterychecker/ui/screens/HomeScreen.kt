package com.unrestricted.batterychecker.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.CheckBox
import androidx.compose.material.icons.rounded.CheckBoxOutlineBlank
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unrestricted.batterychecker.ui.components.AppBatteryCard
import com.unrestricted.batterychecker.ui.components.BatteryDistributionCanvas
import com.unrestricted.batterychecker.ui.components.ExpressiveFilterBar
import com.unrestricted.batterychecker.ui.components.ShizukuStatusBanner
import com.unrestricted.batterychecker.viewmodel.BatteryCheckerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: BatteryCheckerViewModel,
    onNavigateToAbout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val apps by viewModel.filteredApps.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val showSystemApps by viewModel.showSystemApps.collectAsStateWithLifecycle()
    val isShizukuConnected by viewModel.isShizukuConnected.collectAsStateWithLifecycle()
    val isPermissionGranted by viewModel.isShizukuPermissionGranted.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    val shizukuReady = isShizukuConnected && isPermissionGranted

    var showMenu by remember { mutableStateOf(false) }
    var showGuideDialog by remember { mutableStateOf(false) }

    if (showGuideDialog) {
        GuideDialog(onDismiss = { showGuideDialog = false })
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Battery Mode Checker",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Rounded.BatteryChargingFull,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                },
                actions = {
                    // 3-dots overflow menu
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                contentDescription = "Options Menu"
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            // 1. Refresh
                            DropdownMenuItem(
                                text = { Text("Refresh") },
                                leadingIcon = {
                                    Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                                },
                                onClick = {
                                    showMenu = false
                                    viewModel.loadApps()
                                }
                            )

                            // 2. Guide
                            DropdownMenuItem(
                                text = { Text("Guide") },
                                leadingIcon = {
                                    Icon(Icons.Rounded.MenuBook, contentDescription = null, modifier = Modifier.size(20.dp))
                                },
                                onClick = {
                                    showMenu = false
                                    showGuideDialog = true
                                }
                            )

                            // 3. Show system apps (with check state)
                            DropdownMenuItem(
                                text = { Text("Show system apps") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (showSystemApps) Icons.Rounded.CheckBox else Icons.Rounded.CheckBoxOutlineBlank,
                                        contentDescription = null,
                                        tint = if (showSystemApps) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                onClick = {
                                    viewModel.toggleShowSystemApps()
                                }
                            )

                            // 4. About
                            DropdownMenuItem(
                                text = { Text("About") },
                                leadingIcon = {
                                    Icon(Icons.Rounded.Info, contentDescription = null, modifier = Modifier.size(20.dp))
                                },
                                onClick = {
                                    showMenu = false
                                    onNavigateToAbout()
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Shizuku Connection & Privilege Banner with logo and direct download links
            ShizukuStatusBanner(
                isShizukuConnected = isShizukuConnected,
                isPermissionGranted = isPermissionGranted,
                onRequestPermission = { viewModel.requestShizukuPermission() }
            )

            // 2. Native Canvas Battery Distribution Capsule
            BatteryDistributionCanvas(stats = stats)

            // 3. Search Bar (Moved directly above the filters as requested)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                placeholder = { Text("Search by app or package name...") },
                leadingIcon = {
                    Icon(Icons.Rounded.Search, contentDescription = "Search Icon")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Rounded.Clear, contentDescription = "Clear Search")
                        }
                    }
                },
                shape = RoundedCornerShape(18.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            )

            // 4. Expressive Filter Chips Bar (All, Unrestricted, Optimized, Restricted)
            ExpressiveFilterBar(
                selectedFilter = selectedFilter,
                stats = stats,
                onFilterSelected = { viewModel.onFilterSelected(it) }
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 5. App List
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (apps.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (!showSystemApps) "No third-party apps found. Tick 'Show system apps' in the 3-dots menu to view system apps." else "No matching applications found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 32.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = apps,
                        key = { it.packageName }
                    ) { app ->
                        AppBatteryCard(
                            app = app,
                            shizukuReady = shizukuReady,
                            onStateChange = { targetState ->
                                viewModel.setAppOptimizationMode(app, targetState)
                            }
                        )
                    }
                }
            }
        }
    }
}
