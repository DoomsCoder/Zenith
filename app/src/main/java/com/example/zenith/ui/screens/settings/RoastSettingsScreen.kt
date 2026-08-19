package com.example.zenith.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zenith.ui.theme.MutedGray
import com.example.zenith.ui.theme.SoftIndigo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoastSettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val prefs by viewModel.settingsState.collectAsState()
    var showIntensitySheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications & Roasts", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { innerPadding ->
        prefs?.let { p ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                SettingsSectionHeader("ROAST INTENSITY")
                
                // Dropdown-style Selection Box
                IntensitySelector(
                    currentLevel = p.roastIntensity,
                    onClick = { showIntensitySheet = true }
                )

                Spacer(Modifier.height(40.dp))

                SettingsSectionHeader("PUSH THROTTLING")
                
                Text(
                    text = "Minimum gap between push alerts during a session. Prevents notification fatigue.",
                    color = MutedGray,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                
                Spacer(Modifier.height(24.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("GAP INTENSITY", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text(
                        text = "${p.notificationThrottlingSeconds}s",
                        color = SoftIndigo,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                val throttleOptions = listOf(5, 15, 30, 60)
                val currentStep = throttleOptions.indexOf(p.notificationThrottlingSeconds).coerceAtLeast(0).toFloat()

                Slider(
                    value = currentStep,
                    onValueChange = { index ->
                        viewModel.setThrottling(throttleOptions[index.toInt()])
                    },
                    valueRange = 0f..3f,
                    steps = 2,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = SoftIndigo,
                        inactiveTrackColor = Color.DarkGray,
                        activeTickColor = Color.Transparent,
                        inactiveTickColor = Color.Transparent
                    )
                )
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("5s", "15s", "30s", "60s").forEach { label ->
                        Text(label, color = MutedGray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                Spacer(Modifier.height(48.dp))
            }
        }
    }

    if (showIntensitySheet) {
        ModalBottomSheet(
            onDismissRequest = { showIntensitySheet = false },
            containerColor = Color(0xFF111111),
            scrimColor = Color.Black.copy(alpha = 0.8f)
        ) {
            Column(modifier = Modifier.padding(24.dp).padding(bottom = 32.dp)) {
                Text(
                    "CHOOSE INTENSITY",
                    color = SoftIndigo,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(Modifier.height(16.dp))
                
                IntensityOption("None", "Silent mode — no notifications.", prefs?.roastIntensity == 0) {
                    viewModel.setRoastIntensity(0); showIntensitySheet = false
                }
                IntensityOption("Mild", "Gentle nudges when you drift.", prefs?.roastIntensity == 1) {
                    viewModel.setRoastIntensity(1); showIntensitySheet = false
                }
                IntensityOption("Brutal", "Harsh truths, delivered cold.", prefs?.roastIntensity == 2) {
                    viewModel.setRoastIntensity(2); showIntensitySheet = false
                }
                IntensityOption("Savage", "Emotional Damage. You asked for this.", prefs?.roastIntensity == 3) {
                    viewModel.setRoastIntensity(3); showIntensitySheet = false
                }
            }
        }
    }
}

@Composable
private fun IntensitySelector(currentLevel: Int, onClick: () -> Unit) {
    val (title, sub) = when(currentLevel) {
        0 -> "None" to "Silent mode"
        1 -> "Mild" to "Gentle nudges"
        2 -> "Brutal" to "Harsh truths"
        else -> "Savage" to "No mercy"
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        color = Color(0xFF0A0A0A),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(text = "Current Level", color = MutedGray, fontSize = 12.sp)
                Text(text = title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = sub, color = SoftIndigo, fontSize = 13.sp)
            }
            Icon(Icons.Default.KeyboardArrowDown, null, tint = MutedGray)
        }
    }
}

@Composable
private fun IntensityOption(
    title: String,
    desc: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = null,
            colors = RadioButtonDefaults.colors(selectedColor = SoftIndigo, unselectedColor = MutedGray)
        )
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, color = if (isSelected) Color.White else MutedGray, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(desc, color = if (isSelected) SoftIndigo.copy(0.7f) else MutedGray.copy(0.6f), fontSize = 13.sp)
        }
    }
}

@Composable
private fun SettingsSectionHeader(text: String) {
    Text(
        text = text,
        color = SoftIndigo,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        fontFamily = FontFamily.Monospace,
        modifier = Modifier.padding(vertical = 16.dp)
    )
}
