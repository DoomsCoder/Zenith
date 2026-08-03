package com.example.zenith.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
fun EngineConfigScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val prefs by viewModel.settingsState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Engine Config",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
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
                SettingsSectionHeader("ENFORCEMENT")

                SwitchPreference(
                    title = "Auto Do-Not-Disturb",
                    subtitle = "Automatically silence all system notifications when a focus session begins. Zenith alerts will still bypass this shield.",
                    checked = p.isAutoDndEnabled,
                    onCheckedChange = { viewModel.toggleAutoDnd(it) }
                )

                SwitchPreference(
                    title = "Call Emergency Shield",
                    subtitle = "Detect incoming calls and automatically pause the timer and penalties for the duration of the call.",
                    checked = p.isCallShieldEnabled,
                    onCheckedChange = { viewModel.toggleCallShield(it) }
                )

                Spacer(Modifier.height(32.dp))

                SettingsSectionHeader("MERCY BUFFER")

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Free violations per session", color = Color.White, fontSize = 16.sp)
                        Text(
                            "Number of free pickups/switches allowed before roasts and penalties activate.",
                            color = MutedGray,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                    Text(
                        text = p.mercyBuffer.toString(),
                        color = SoftIndigo,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Slider(
                    value = p.mercyBuffer.toFloat(),
                    onValueChange = { viewModel.setMercyBuffer(it.toInt()) },
                    valueRange = 0f..3f,
                    steps = 2,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = SoftIndigo,
                        inactiveTrackColor = Color.DarkGray
                    )
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("0", "1", "2", "3").forEach { label ->
                        Text(label, color = MutedGray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }

                Spacer(Modifier.height(40.dp))

                SettingsSectionHeader("STRICTNESS LEVEL")
                Text(
                    "How aggressively Zenith enforces your session. Affects penalties, lock-outs, and roast triggers.",
                    color = MutedGray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                StrictnessSelector(
                    selectedLevel = p.strictnessLevel,
                    onLevelSelected = { viewModel.setStrictness(it) }
                )

                Spacer(Modifier.height(48.dp))
            }
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

@Composable
private fun SwitchPreference(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = MutedGray, fontSize = 13.sp, lineHeight = 18.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SoftIndigo,
                uncheckedThumbColor = MutedGray,
                uncheckedTrackColor = Color(0xFF1A1A1A)
            )
        )
    }
}

@Composable
private fun StrictnessSelector(
    selectedLevel: Int,
    onLevelSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(Color(0xFF0A0A0A), RoundedCornerShape(12.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StrictnessOption(
            title = "Low",
            subtitle = "Standard",
            isSelected = selectedLevel == 0,
            modifier = Modifier.weight(1f),
            onClick = { onLevelSelected(0) }
        )
        StrictnessOption(
            title = "High",
            subtitle = "Zenith",
            isSelected = selectedLevel == 1,
            modifier = Modifier.weight(1f),
            onClick = { onLevelSelected(1) }
        )
        StrictnessOption(
            title = "Merciless",
            subtitle = "No recovery",
            isSelected = selectedLevel == 2,
            modifier = Modifier.weight(1f),
            onClick = { onLevelSelected(2) }
        )
    }
}

@Composable
private fun StrictnessOption(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(
                if (isSelected) SoftIndigo.copy(alpha = 0.15f) else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = if (isSelected) SoftIndigo else Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = if (isSelected) SoftIndigo.copy(0.7f) else MutedGray, fontSize = 10.sp)
        }
    }
}