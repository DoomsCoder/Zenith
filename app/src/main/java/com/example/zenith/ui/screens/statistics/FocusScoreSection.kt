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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
    score: Int,
    weeklyDelta: Int,
    currentStreak: Int,
    bestStreak: Int,
    isStreakLost: Boolean,
    tierProgress: Float,
    currentTierLabel: String,
    breakdown: ScoreBreakdown,
    onRecoveryClick: () -> Unit,
    onShowRules: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Focus score",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = SoftIndigo
                )
            )
            Spacer(Modifier.width(12.dp))
            Surface(
                onClick = onShowRules,
                shape = CircleShape,
                color = SoftIndigo.copy(0.12f),
                border = BorderStroke(1.dp, SoftIndigo.copy(0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Info, null, Modifier.size(14.dp), tint = SoftIndigo)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Rules",
                        style = MaterialTheme.typography.labelSmall.copy(
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
            style = MaterialTheme.typography.bodySmall.copy(color = deltaColor)
        )
        Text(
            String.format("%,d", score),
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Light,
                fontSize = 64.sp,
                color = Color.White
            )
        )
        Text(
            currentTierLabel,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = SoftIndigo
            )
        )
    }

    Spacer(modifier = Modifier.height(32.dp))
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            val percentage = (tierProgress * 100).toInt()
            Text(
                "$percentage% to next tier",
                style = MaterialTheme.typography.labelSmall.copy(color = MutedGray)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { tierProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = SoftIndigo,
            trackColor = Color.White.copy(0.05f),
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }

    Spacer(modifier = Modifier.height(40.dp))

    if (!isStreakLost) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🔥", fontSize = 24.sp)
                    Text(
                        "$currentStreak days",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        "Current streak",
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
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        "Personal best",
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
                        "Streak lost",
                        color = MutedGray,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(Modifier.height(20.dp))
                TelemetryRow("Current", "0 days", valueColor = MutedGray)
                TelemetryRow(
                    "Record",
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
                Button(
                    onClick = onRecoveryClick,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(100.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SoftIndigo)
                ) {
                    Text(
                        "Begin recovery session",
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

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
                    "Score breakdown",
                    style = MaterialTheme.typography.bodyMedium.copy(
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
                TelemetryRow("Sessions completed", "+${breakdown.completionPoints} pts", valueColor = SoftIndigo)
                TelemetryRow("Focus minutes", "+${breakdown.focusMinutePoints} pts", valueColor = SoftIndigo)
                TelemetryRow("Abandonments", "-${breakdown.abandonmentPenalty} pts", valueColor = Color(0xFFEF5350))
                TelemetryRow("Pickups detected", "-${breakdown.pickupPenalty} pts", valueColor = Color(0xFFEF5350))
                TelemetryRow("App switches", "-${breakdown.appSwitchPenalty} pts", valueColor = Color(0xFFEF5350))
                TelemetryRow("Streak bonus", "+${breakdown.streakBonus} pts", valueColor = SoftIndigo)
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(0.05f))
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Total",
                        style = MaterialTheme.typography.labelSmall.copy(color = MutedGray)
                    )
                    Text(
                        String.format("%,d pts", score),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
        }
    }
}
