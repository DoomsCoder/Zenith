package com.example.zenith.ui.screens.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zenith.R
import com.example.zenith.ui.theme.MutedGray
import com.example.zenith.ui.theme.SoftIndigo
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutZenithScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showChangelog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About", color = Color.White, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))

            // App Icon
            Surface(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF1A1A1A)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "Zenith Icon",
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "Zenith Focus Engine",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
            
            Text(
                "Version 1.0.0-beta (2a4f6d8)",
                color = MutedGray,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("d MMM yyyy")),
                color = MutedGray,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(40.dp))

            AboutSection(
                title = "Mission",
                description = "Zenith is a high-reliability focus enforcer designed to reclaim human attention through strict telemetry and aggressive accountability. Engineered for deep work."
            )

            AboutSection(
                title = "License",
                description = "Zenith is released under the Apache License 2.0. The source code is open and verifiable. Zenith also uses third-party libraries and icons licensed under various open source licenses."
            )

            AboutSection(
                title = "Connect",
                description = "Zenith is developed by Vedant Kakade. You can reach out for bug reports, feature requests, or collaboration."
            )

            Spacer(Modifier.height(32.dp))

            // Action Links
            BlueActionLink("Rate Zenith") {
                Toast.makeText(context, "Rating will be enabled upon public release.", Toast.LENGTH_SHORT).show()
            }
            
            BlueActionLink("Open changelog") {
                showChangelog = true
            }
            
            BlueActionLink("Copy debug info") {
                copyDebugInfoToClipboard(context)
            }

            Spacer(Modifier.height(48.dp))
        }
    }

    if (showChangelog) {
        AlertDialog(
            onDismissRequest = { showChangelog = false },
            containerColor = Color(0xFF111111),
            title = { Text("Changelog", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "v1.0.0-beta\n\n- Initial release of Zenith Focus Engine\n- Deep Focus monitoring system\n- Time Debt & Penalty engine\n- Whitelist manager\n- Sensory punishment suite\n- Mission telemetry dashboard",
                    color = MutedGray,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { showChangelog = false }) {
                    Text("Close", color = SoftIndigo, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun AboutSection(title: String, description: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            title,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            description,
            color = MutedGray,
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun BlueActionLink(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        color = Color(0xFF64B5F6),
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 12.dp)
            .fillMaxWidth(),
        textAlign = TextAlign.Center
    )
}

private fun copyDebugInfoToClipboard(context: Context) {
    val buildDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
    val debugInfo = """
        Zenith Version = 1.0.0-beta (Build $buildDate)
        Android Version = ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})
        Device = ${Build.MANUFACTURER} | ${Build.MODEL}
        Engine Status = Operational
        Database Version = 3
    """.trimIndent()

    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Zenith Debug Info", debugInfo)
    clipboard.setPrimaryClip(clip)

    Toast.makeText(context, "Debug info copied to clipboard", Toast.LENGTH_SHORT).show()
}
