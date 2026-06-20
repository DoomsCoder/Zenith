package com.example.zenith

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.zenith.service.FocusService
import com.example.zenith.ui.components.ZenithBottomBar
import com.example.zenith.ui.components.ZenithTopAppBar
import com.example.zenith.ui.navigation.Destination
import com.example.zenith.ui.screens.focus.FocusScreen
import com.example.zenith.ui.screens.focus.FocusViewModel
import com.example.zenith.ui.screens.settings.SettingsScreen
import com.example.zenith.ui.screens.statistics.StatisticsScreen
import com.example.zenith.ui.theme.ZenithTheme

class MainActivity : ComponentActivity() {

    private val serviceIntent by lazy {
        Intent(this, FocusService::class.java)
    }
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
                        if (backStack.last() != Destination.Settings) {
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
                                Destination.Stats -> StatisticsScreen()
                                Destination.Settings -> SettingsScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}
