package com.example.zenith.ui.screens.settings

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.zenith.service.VibrationManager
import com.example.zenith.ui.theme.MutedGray
import com.example.zenith.ui.theme.SoftIndigo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorySettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val prefs by viewModel.settingsState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showPatternSheet by remember { mutableStateOf(false) }

    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION") context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(Color(0xFF121212)).statusBarsPadding()) {
                Spacer(Modifier.height(16.dp))
                TopAppBar(
                    title = { 
                        Text(
                            "Sensory Punishment", 
                            color = Color.White, 
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium)
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        },
        containerColor = Color(0xFF121212)
    ) { innerPadding ->
        prefs?.let { p ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(16.dp))
                
                SettingsSectionHeader("Physical enforcement")

                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Haptic Punishment", color = Color.White, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                        Text("Vibrate the device when distractions are detected.", color = MutedGray, style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(
                        checked = p.isHapticsEnabled,
                        onCheckedChange = { viewModel.toggleHaptics(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = SoftIndigo
                        )
                    )
                }

                Spacer(Modifier.height(32.dp))

                SettingsSectionHeader("Vibration strength")
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Intensity", color = Color.White, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = "${p.vibrationStrength}%",
                        color = if (p.isHapticsEnabled) SoftIndigo else MutedGray,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }

                Slider(
                    value = p.vibrationStrength.toFloat(),
                    onValueChange = { viewModel.setVibrationStrength(it.toInt()) },
                    valueRange = 0f..100f,
                    enabled = p.isHapticsEnabled,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = SoftIndigo,
                        inactiveTrackColor = Color.DarkGray
                    ),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(Modifier.height(32.dp))

                SettingsSectionHeader("Punishment pattern")
                
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = p.isHapticsEnabled) { showPatternSheet = true },
                    color = Color(0xFF0A0A0A),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Current Pattern", color = MutedGray, style = MaterialTheme.typography.labelSmall)
                            Text(
                                text = VibrationManager.getPatternName(p.vibrationPattern),
                                color = if (p.isHapticsEnabled) Color.White else MutedGray,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Icon(Icons.Default.KeyboardArrowDown, null, tint = MutedGray)
                    }
                }

                Spacer(Modifier.height(48.dp))

                Button(
                    onClick = {
                        val pattern = VibrationManager.getPattern(p.vibrationPattern)
                        val amplitudes = IntArray(pattern.size) { i ->
                            if (i % 2 == 0) 0 else (p.vibrationStrength * 2.55f).toInt().coerceIn(1, 255)
                        }
                        vibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1A1A)),
                    shape = RoundedCornerShape(12.dp),
                    enabled = p.isHapticsEnabled
                ) {
                    Text("Test punishment", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                }

                if (p.vibrationStrength > 80) {
                    Row(
                        modifier = Modifier.padding(top = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, null, tint = Color(0xFFFFB74D), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "High intensity may drain battery faster.",
                            color = Color(0xFFFFB74D),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                Spacer(Modifier.height(48.dp))
            }
        }
    }

    if (showPatternSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPatternSheet = false },
            containerColor = Color(0xFF111111)
        ) {
            Column(modifier = Modifier.padding(24.dp).padding(bottom = 32.dp)) {
                Text(
                    "Choose pattern", 
                    color = SoftIndigo, 
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(Modifier.height(16.dp))
                
                (0..4).forEach { index ->
                    PatternOption(
                        title = VibrationManager.getPatternName(index),
                        isSelected = prefs?.vibrationPattern == index,
                        onClick = { viewModel.setVibrationPattern(index); showPatternSheet = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun PatternOption(title: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = isSelected, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = SoftIndigo))
        Spacer(Modifier.width(16.dp))
        Text(title, color = if (isSelected) Color.White else MutedGray, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
    }
}

@Composable
private fun SettingsSectionHeader(text: String) {
    Text(
        text = text,
        color = SoftIndigo,
        style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        ),
        modifier = Modifier.padding(vertical = 16.dp)
    )
}
