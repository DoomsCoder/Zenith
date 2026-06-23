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
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.zenith.ui.components.ZenithBottomBar
import com.example.zenith.ui.components.ZenithTopAppBar
import com.example.zenith.ui.navigation.Destination
import com.example.zenith.ui.screens.focus.FocusScreen
import com.example.zenith.ui.screens.focus.FocusViewModel
import com.example.zenith.ui.screens.settings.SettingsScreen
import com.example.zenith.ui.screens.statistics.SessionHistoryScreen
import com.example.zenith.ui.screens.statistics.StatisticsScreen
import com.example.zenith.ui.theme.ZenithTheme

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZenithTheme {

                val navKeyBackStack = rememberNavBackStack(Destination.Focus)

                @Suppress("UNCHECKED_CAST")
                val backStack = navKeyBackStack as NavBackStack<Destination>

                val focusViewModel: FocusViewModel = viewModel(
                    viewModelStoreOwner = LocalViewModelStoreOwner.current!!
                )

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets(0,0,0,0),
                    topBar = {
                        ZenithTopAppBar(
                            onNavigateToSettings = {
                                if (backStack.last() != Destination.Settings){
                                    backStack.add(Destination.Settings)
                                }
                            }
                        )
                    },
                    bottomBar = {
                        if (backStack.last() != Destination.Settings && backStack.last() != Destination.SessionHistory) {
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
                            .padding(top = innerPadding.calculateTopPadding())

                    ) { key ->

                        NavEntry(key) {
                            when (key) {
                                Destination.Focus -> FocusScreen(viewModel = focusViewModel)
                                Destination.Stats -> StatisticsScreen(
                                    onNavigateToHistory = { backStack.add(Destination.SessionHistory) }
                                )
                                Destination.Settings -> SettingsScreen()
                                Destination.SessionHistory -> SessionHistoryScreen(
                                    onBackClick = { backStack.remove(Destination.SessionHistory) }
                                )
                            }
                        }
                    }
                }
            }
        }
        // Request notification permission (Standard popup)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }

        // Initial check: If they don't have it, send them to settings
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
