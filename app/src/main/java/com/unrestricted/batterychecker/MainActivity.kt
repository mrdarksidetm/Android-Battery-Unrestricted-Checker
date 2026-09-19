package com.unrestricted.batterychecker

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.unrestricted.batterychecker.ui.screens.AboutScreen
import com.unrestricted.batterychecker.ui.screens.HomeScreen
import com.unrestricted.batterychecker.ui.theme.AndroidBatteryUnrestrictedCheckerTheme
import com.unrestricted.batterychecker.viewmodel.BatteryCheckerViewModel
import rikka.shizuku.Shizuku

class MainActivity : ComponentActivity() {

    private val viewModel: BatteryCheckerViewModel by viewModels()

    private val requestPermissionResultListener =
        Shizuku.OnRequestPermissionResultListener { _, grantResult ->
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                viewModel.checkShizukuState()
                viewModel.loadApps()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            Shizuku.addRequestPermissionResultListener(requestPermissionResultListener)
        } catch (e: Throwable) {
            // Shizuku not present
        }

        setContent {
            AndroidBatteryUnrestrictedCheckerTheme {
                var currentScreen by remember { mutableStateOf("home") }

                if (currentScreen == "about") {
                    BackHandler { currentScreen = "home" }
                    AboutScreen(onBack = { currentScreen = "home" })
                } else {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateToAbout = { currentScreen = "about" }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            Shizuku.removeRequestPermissionResultListener(requestPermissionResultListener)
        } catch (e: Throwable) {
            // Safe cleanup
        }
    }
}
