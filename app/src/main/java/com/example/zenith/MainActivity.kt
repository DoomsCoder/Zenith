package com.example.zenith

import android.Manifest
import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.zenith.animation.setupSplashScreenExitAnimation
import com.example.zenith.ui.components.ZenithBottomBar
import com.example.zenith.ui.components.ZenithTopAppBar
import com.example.zenith.ui.navigation.Destination
import com.example.zenith.ui.screens.focus.FocusScreen
import com.example.zenith.ui.screens.focus.FocusViewModel
import com.example.zenith.ui.screens.settings.EngineConfigScreen
import com.example.zenith.ui.screens.settings.RoastSettingsScreen
import com.example.zenith.ui.screens.settings.SensorySettingsScreen
import com.example.zenith.ui.screens.settings.AboutZenithScreen
import com.example.zenith.ui.screens.settings.DataPrivacyScreen
import com.example.zenith.ui.screens.settings.SettingsScreen
import com.example.zenith.ui.screens.settings.SettingsViewModel
import com.example.zenith.ui.screens.settings.WhitelistManagerScreen
import com.example.zenith.ui.screens.settings.WhitelistViewModel
import com.example.zenith.ui.screens.statistics.SessionHistoryScreen
import com.example.zenith.ui.screens.statistics.StatisticsScreen
import com.example.zenith.ui.screens.statistics.StatisticsViewModel
import com.example.zenith.ui.theme.ZenithTheme

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        setupSplashScreenExitAnimation(splashScreen)
        enableEdgeToEdge()
        setContent {
            ZenithTheme {

                val navKeyBackStack = rememberNavBackStack(Destination.Focus)

                @Suppress("UNCHECKED_CAST")
                val backStack = navKeyBackStack as NavBackStack<Destination>

                val focusViewModel: FocusViewModel = viewModel(
                    viewModelStoreOwner = LocalViewModelStoreOwner.current!!
                )

                val statsViewModel: StatisticsViewModel = viewModel(
                    viewModelStoreOwner = LocalViewModelStoreOwner.current!!
                )

                val settingsViewModel: SettingsViewModel = viewModel(
                    viewModelStoreOwner = LocalViewModelStoreOwner.current!!
                )

                val whitelistViewModel: WhitelistViewModel = viewModel(
                    viewModelStoreOwner = LocalViewModelStoreOwner.current!!
                )

                val currentDestination = backStack.last()
                val showBars = currentDestination.showSystemBars

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets(0,0,0,0),
                    topBar = {
                        if (showBars) {
                            ZenithTopAppBar(
                                onNavigateToSettings = {
                                    if (backStack.last() != Destination.Settings) {
                                        backStack.add(Destination.Settings)
                                    }
                                },
                                onNavigateToAbout = {
                                    if (backStack.last() != Destination.AboutZenith) {
                                        backStack.add(Destination.AboutZenith)
                                    }
                                }
                            )
                        }
                    },
                    bottomBar = {
                        if (showBars) {
                            ZenithBottomBar(
                                currentDestination = backStack.last(),
                                onNavigate = { newDestination ->

                                    if (backStack.last() != newDestination) {
                                        backStack.clear()
                                        backStack.add(newDestination)
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->

                    NavDisplay(
                        backStack = backStack,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = if (showBars) innerPadding.calculateTopPadding() else 0.dp)
                    ) { key ->
                        NavEntry(key) {
                            when (key) {
                                Destination.Focus -> FocusScreen(viewModel = focusViewModel)
                                Destination.Stats -> StatisticsScreen(
                                    viewModel = statsViewModel,
                                    onNavigateToHistory = { backStack.add(Destination.SessionHistory) },
                                    onNavigateToFocus = {
                                        backStack.clear()
                                        backStack.add(Destination.Focus)
                                    }
                                )
                                Destination.Settings -> SettingsScreen(
                                    onCategoryClick = { categoryId ->
                                        when(categoryId) {
                                            "engine" -> backStack.add(Destination.EngineConfig)
                                            "whitelist" -> backStack.add(Destination.Whitelist)
                                            "notifications" -> backStack.add(Destination.Roasts)
                                            "sensory" -> backStack.add(Destination.Sensory)
                                            "data" -> backStack.add(Destination.DataPrivacy)
                                            "about" -> backStack.add(Destination.AboutZenith)
                                        }
                                    },
                                    onBack = { backStack.remove(Destination.Settings) }
                                )
                                Destination.EngineConfig -> {
                                    EngineConfigScreen(
                                        viewModel = settingsViewModel,
                                        onBack = { backStack.remove(Destination.EngineConfig)}
                                    )
                                }
                                Destination.Roasts -> {
                                    RoastSettingsScreen(
                                        viewModel = settingsViewModel,
                                        onBack = { backStack.remove(Destination.Roasts) }
                                    )
                                }
                                Destination.Sensory -> {
                                    SensorySettingsScreen(
                                        viewModel = settingsViewModel,
                                        onBack = { backStack.remove(Destination.Sensory) }
                                    )
                                }
                                Destination.DataPrivacy -> {
                                    DataPrivacyScreen(
                                        viewModel = settingsViewModel,
                                        onBack = { backStack.remove(Destination.DataPrivacy) }
                                    )
                                }
                                Destination.Whitelist -> {
                                    WhitelistManagerScreen(
                                        viewModel = whitelistViewModel,
                                        onBack = { backStack.remove(Destination.Whitelist) }
                                    )
                                }
                                Destination.SessionHistory -> SessionHistoryScreen(
                                    viewModel = statsViewModel,
                                    onBackClick = { backStack.remove(Destination.SessionHistory) }
                                )
                                Destination.AboutZenith -> AboutZenithScreen(
                                    onBack = { backStack.remove(Destination.AboutZenith) }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }

        requestUsageStatsPermission()
    }
    @SuppressLint("ServiceCast")
    private fun hasUsageStatsPermission(): Boolean {

        val appOps = getSystemService(APP_OPS_SERVICE) as AppOpsManager

        val mode =if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                packageName
            )
        }

        return mode == AppOpsManager.MODE_ALLOWED
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun requestUsageStatsPermission() {
        if (!hasUsageStatsPermission()) {

            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {

                data = Uri.fromParts("package",packageName,null)
            }

            startActivity(intent)
        }
    }
}
