package com.example.zenith.ui.screens.settings

import android.app.NotificationManager
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
    val notificationManager = context.getSystemService(NotificationManager::class.java)

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Engine Config",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF121212))
            )
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
                
                SettingsSectionHeader("Enforcement")

                SwitchPreference(
                    title = "Auto Do-Not-Disturb",
                    subtitle = "Use Android's Priority-only Do Not Disturb while a focus session is active.",
                    checked = p.isAutoDndEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled && !notificationManager.isNotificationPolicyAccessGranted) {
                            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
                        } else {
                            viewModel.toggleAutoDnd(enabled)
                        }
                    }
                )

                SwitchPreference(
                    title = "Call Emergency Shield",
                    subtitle = "Automatically pause the timer and penalties during active phone calls.",
                    checked = p.isCallShieldEnabled,
                    onCheckedChange = { viewModel.toggleCallShield(it) }
                )

                Spacer(Modifier.height(32.dp))

                SettingsSectionHeader("Mercy buffer")

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Free violations per session", color = Color.White, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Number of free pickups/switches allowed before penalties activate.",
                            color = MutedGray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(
                        text = p.mercyBuffer.toString(),
                        color = SoftIndigo,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
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
                    ),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("0", "1", "2", "3").forEach { label ->
                        Text(label, color = MutedGray, style = MaterialTheme.typography.labelSmall)
                    }
                }

                Spacer(Modifier.height(40.dp))

                SettingsSectionHeader("Strictness level")
                Text(
                    "How aggressively Zenith enforces your session. Affects penalties and roast triggers.",
                    color = MutedGray,
                    style = MaterialTheme.typography.bodyMedium,
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
        style = MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        ),
        modifier = Modifier.padding(bottom = 12.dp)
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
            Text(title, color = Color.White, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
            Text(subtitle, color = MutedGray, style = MaterialTheme.typography.bodySmall)
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
            .background(Color(0xFF0A0A0A), RoundedCornerShape(16.dp)),
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
                RoundedCornerShape(16.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = if (isSelected) SoftIndigo else Color.White, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
            Text(subtitle, color = if (isSelected) SoftIndigo.copy(0.7f) else MutedGray, style = MaterialTheme.typography.labelSmall)
        }
    }
}
