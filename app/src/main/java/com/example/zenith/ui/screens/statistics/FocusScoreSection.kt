package com.example.zenith.ui.screens.statistics

import android.annotation.SuppressLint
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zenith.ui.theme.MutedGray
import com.example.zenith.ui.theme.OffWhite
import com.example.zenith.ui.theme.SoftIndigo

@SuppressLint("DefaultLocale")
@Composable
fun FocusScoreSection(
    score: Int = 2297,
    weeklyDelta: Int = 340,
    currentStreak: Int = 12,
    bestStreak: Int = 15,
    isStreakLost: Boolean = false,
    onShowRules: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Hero Section Area
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "FOCUS SCORE",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = MutedGray.copy(0.7f)
                )
            )
            Spacer(Modifier.width(12.dp))
            Surface(
                onClick = onShowRules,
                shape = CircleShape,
                color = SoftIndigo.copy(0.15f),
                border = BorderStroke(
                    1.dp,
                    SoftIndigo.copy(0.3f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Info, null, Modifier.size(12.dp), tint = SoftIndigo)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "RULES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SoftIndigo
                        )
                    )
                }
            }
        }
    }


    Spacer(modifier = Modifier.height(32.dp))

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        val deltaPrefix = if (weeklyDelta >= 0) "▲ +" else "▼ "
        val deltaColor = if (weeklyDelta >= 0) Color(0xFF4CAF50) else Color(0xFFEF5350)
        Text(
            "$deltaPrefix$weeklyDelta pts this week",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                color = deltaColor
            )
        )
        Text(
            String.format("%,d", score),
            style = MaterialTheme.typography.displayLarge.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 56.sp,
                color = Color.White
            )
        )
        Text(
            "── DEEP WORKER ──",
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                letterSpacing = 4.sp,
                fontWeight = FontWeight.Bold,
                color = SoftIndigo
            )
        )
    }

    Spacer(modifier = Modifier.height(32.dp))
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                "56% to FLOW STATE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    color = MutedGray.copy(0.6f)
                )
            )
            Text(
                "NEXT TIER",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    color = MutedGray.copy(0.4f)
                )
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { 0.56f }, modifier = Modifier
                .fillMaxWidth()
                .height(2.dp), color = SoftIndigo, trackColor = Color.White.copy(0.05f)
        )
    }

    Spacer(modifier = Modifier.height(40.dp))

    // Streak Module
    if (!isStreakLost) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp), verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🔥", fontSize = 24.sp)
                    Text(
                        "$currentStreak days",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        "CURRENT STREAK",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedGray
                    )
                }
                VerticalDivider(modifier = Modifier.height(40.dp), color = Color.White.copy(0.1f))
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("⚡", fontSize = 24.sp)
                    Text(
                        "$bestStreak days",
                        color = MutedGray,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        "PERSONAL BEST",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedGray.copy(0.5f)
                    )
                }
            }
        }
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(MutedGray, CircleShape)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "STREAK LOST",
                        color = MutedGray,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(Modifier.height(20.dp))
                TelemetryRow("CURRENT", "0 days", valueColor = MutedGray)
                TelemetryRow(
                    "RECORD",
                    "⚡ $bestStreak-day best intact",
                    valueColor = SoftIndigo.copy(0.7f)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Start a new chain today.",
                    color = MutedGray.copy(0.6f),
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(24.dp))
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, SoftIndigo.copy(0.3f))
                ) {
                    Text(
                        " BEGIN RECOVERY SESSION ",
                        color = SoftIndigo,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // --- 3. SCORE BREAKDOWN ---
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color.White.copy(0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "SCORE BREAKDOWN",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        color = OffWhite.copy(0.7f)
                    )
                )
                Icon(
                    if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    null,
                    tint = MutedGray.copy(0.4f),
                    modifier = Modifier.size(16.dp)
                )
            }
            if (expanded) {
                Spacer(Modifier.height(24.dp))
                TelemetryRow("Sessions Completed", "+1,200 pts", valueColor = SoftIndigo)
                TelemetryRow("Focus Minutes", "+847 pts", valueColor = SoftIndigo)
                TelemetryRow("Abandonments", "-150 pts", valueColor = Color(0xFFEF5350))
                TelemetryRow("Pickups Detected", "-50 pts", valueColor = Color(0xFFEF5350))
                TelemetryRow("App Switches", "-50 pts", valueColor = Color(0xFFEF5350))
                TelemetryRow("Streak Bonus", "+500 pts", valueColor = SoftIndigo)
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(0.05f))
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "TOTAL",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = MutedGray
                        )
                    )
                    Text(
                        String.format("%,d pts", score),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
        }
    }
}

