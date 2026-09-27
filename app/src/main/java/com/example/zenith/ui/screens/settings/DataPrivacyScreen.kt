package com.example.zenith.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.zenith.ui.theme.MutedGray
import com.example.zenith.ui.theme.SoftIndigo

enum class DataAction { PURGE_HISTORY, RESET_ENGINE, FACTORY_RESET }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataPrivacyScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val totalSessions by viewModel.totalSessions.collectAsStateWithLifecycle()
    val totalPickups by viewModel.totalPickups.collectAsStateWithLifecycle()
    val totalAppSwitches by viewModel.totalAppSwitches.collectAsStateWithLifecycle()
    
    var showConfirmAction by remember { mutableStateOf<DataAction?>(null) }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(Color(0xFF121212)).statusBarsPadding()) {
                Spacer(Modifier.height(16.dp))
                TopAppBar(
                    title = { 
                        Text(
                            "Data & Privacy", 
                            color = Color.White, 
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Medium
                            )
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))
            
            SettingsSectionHeader("Mission telemetry")
            
            TelemetryDashboard(
                sessions = totalSessions,
                distractions = totalPickups + totalAppSwitches
            )

            Spacer(Modifier.height(32.dp))

            SettingsSectionHeader("System actions")
            
            ActionRow(
                title = "Purge mission history",
                subtitle = "Delete all session records and distraction logs.",
                icon = Icons.Default.History,
                onClick = { showConfirmAction = DataAction.PURGE_HISTORY }
            )

            ActionRow(
                title = "Reset engine config",
                subtitle = "Revert all parameters to factory state.",
                icon = Icons.Default.Refresh,
                onClick = { showConfirmAction = DataAction.RESET_ENGINE }
            )

            ActionRow(
                title = "Complete factory reset",
                subtitle = "Total wipe of history, whitelist, and settings.",
                icon = Icons.Default.DeleteForever,
                isCritical = true,
                onClick = { showConfirmAction = DataAction.FACTORY_RESET }
            )

            Spacer(Modifier.height(32.dp))

            SettingsSectionHeader("Zenith privacy protocol")
            
            Text(
                text = "Zenith operates as a closed-loop system. Telemetry never leaves local encrypted storage. No cloud hooks. No external tracking. Zero data leakage.",
                color = MutedGray,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 20.sp
            )
            
            Spacer(Modifier.height(48.dp))
        }
    }

    showConfirmAction?.let { action ->
        ZenithConfirmDialog(
            action = action,
            onDismiss = { showConfirmAction = null },
            onConfirm = {
                when(action) {
                    DataAction.PURGE_HISTORY -> viewModel.clearMissionHistory()
                    DataAction.RESET_ENGINE -> viewModel.resetEngineConfig()
                    DataAction.FACTORY_RESET -> viewModel.factoryReset()
                }
                showConfirmAction = null
            }
        )
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
private fun TelemetryDashboard(sessions: Int, distractions: Int) {
    Surface(
        color = Color(0xFF0A0A0A),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Missions", color = MutedGray, style = MaterialTheme.typography.labelSmall)
                Text("$sessions", color = Color.White, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Light))
            }
            Box(Modifier.width(1.dp).height(40.dp).background(Color.White.copy(0.05f)).align(Alignment.CenterVertically))
            Column {
                Text("Violations", color = MutedGray, style = MaterialTheme.typography.labelSmall)
                Text("$distractions", color = Color.White, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Light))
            }
            Box(Modifier.width(1.dp).height(40.dp).background(Color.White.copy(0.05f)).align(Alignment.CenterVertically))
            Column(horizontalAlignment = Alignment.End) {
                Text("Status", color = MutedGray, style = MaterialTheme.typography.labelSmall)
                Text("SECURE", color = Color(0xFF4CAF50), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(top = 8.dp))
            }
        }
    }
}

@Composable
private fun ActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isCritical: Boolean = false,
    onClick: () -> Unit
) {
    val contentColor = if (isCritical) Color(0xFFEF5350) else Color.White
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = contentColor.copy(alpha = 0.6f), modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title, 
                color = contentColor, 
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                subtitle, 
                color = MutedGray, 
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun ZenithConfirmDialog(
    action: DataAction,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = Color(0xFF111111),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, Color.White.copy(0.05f)),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    if (action == DataAction.FACTORY_RESET) Icons.Default.DeleteForever else Icons.Default.Warning,
                    null,
                    tint = if (action == DataAction.FACTORY_RESET) Color(0xFFEF5350) else SoftIndigo,
                    modifier = Modifier.size(32.dp)
                )
                
                Spacer(Modifier.height(16.dp))
                
                Text(
                    "System overwrite",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                
                Spacer(Modifier.height(12.dp))
                
                Text(
                    text = when(action) {
                        DataAction.PURGE_HISTORY -> "Purging focus telemetry records. This action is final."
                        DataAction.RESET_ENGINE -> "Restoring engine parameters to factory defaults."
                        DataAction.FACTORY_RESET -> "Total system wipe initiated. All mission data and module permissions will be erased."
                    },
                    color = MutedGray,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(Modifier.height(32.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(100.dp),
                        border = BorderStroke(1.dp, Color.White.copy(0.12f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Text("Cancel", style = MaterialTheme.typography.labelLarge)
                    }
                    
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (action == DataAction.FACTORY_RESET) Color(0xFFD32F2F) else SoftIndigo
                        ),
                        shape = RoundedCornerShape(100.dp)
                    ) {
                        Text(
                            text = if (action == DataAction.FACTORY_RESET) "Erase" else "Confirm",
                            color = Color.White,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
