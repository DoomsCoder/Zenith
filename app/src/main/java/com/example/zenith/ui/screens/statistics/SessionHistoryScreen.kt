package com.example.zenith.ui.screens.statistics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zenith.ui.theme.SoftIndigo

data class SessionHistoryItem(
    val id: String,
    val dataTimeStr: String,
    val title: String,
    val durationMinutes: Int,
    val isCompleted: Boolean,
    val pickups: Int,
    val appSwitches: Int,
    val scoreImpact: Int
)

@Composable
fun SessionHistoryScreen(
    onBackClick: () -> Unit
) {
    // Dummy Data
    val allSessions = remember {
        listOf(
            SessionHistoryItem("1", "JUN 21 • 5:50 PM", "AnkiDroid PR #20849", 25, true, 0, 1, 130),
            SessionHistoryItem("2", "JUN 21 • 3:15 PM", "Chapter 4 Reading", 50, false, 3, 5, -20),
            SessionHistoryItem("3", "JUN 20 • 9:00 AM", "System Architecture", 45, true, 0, 0, 200)
        )
    }

    var selectedFilter by rememberSaveable { mutableStateOf("All") }
    val filteredSessions = remember(selectedFilter){
        val list = when(selectedFilter) {
            "Completed" -> allSessions.filter { it.isCompleted }
            "Abandoned" -> allSessions.filter { !it.isCompleted }
            else -> allSessions
        }

        list.reversed()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A)) // Pure Dark
    ) {
        // Top Navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, start = 16.dp, end = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = "SESSION HISTORY",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = Color.White
                )
            )
        }

        Spacer(Modifier.height(20.dp))

        // Filters Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All","Completed","Abandoned").forEach { label ->
                    val isSelected = selectedFilter == label
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = label },
                        label = { Text(label, style = MaterialTheme.typography.labelSmall)},
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color.Transparent,
                            selectedContainerColor = SoftIndigo.copy(0.1f),
                            labelColor = Color.Gray,
                            selectedLabelColor = SoftIndigo
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = Color.DarkGray,
                            selectedBorderColor = SoftIndigo,
                            borderWidth = 1.dp,
                            selectedBorderWidth = 1.dp
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            Text(
                text = "${filteredSessions.size} sessions",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    color = Color.DarkGray
                )
            )
        }

        Spacer(Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(start = 24.dp, top = 8.dp, end = 24.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items = filteredSessions, key = { it.id }) { session ->
                SessionHistoryCard(session)
            }
        }
    }
}

@Composable
private fun SessionHistoryCard(item: SessionHistoryItem) {
    var isExpanded by remember {mutableStateOf(false)}

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { isExpanded = !isExpanded },
        color = Color(0xFF141414), // Dark Charcoal
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header: Date/Time
            Text(
                text = item.dataTimeStr.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
            Spacer(Modifier.height(6.dp))
            // Title
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
            )

            Spacer(Modifier.height(20.dp))

            // Duration Row
            HistoryMetricRow(
                label = "DURATION",
                value = "${item.durationMinutes} min",
                valueColor = Color.White
            )

            Spacer(Modifier.height(10.dp))

            // Status Row
            val statusText = if (item.isCompleted) "✓ Completed" else "✕ Abandoned"
            val statusColor = if (item.isCompleted) SoftIndigo else Color.Gray
            HistoryMetricRow(
                label = "STATUS",
                value = statusText,
                valueColor = statusColor
            )

            // Telemetry Expansion
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    Spacer(Modifier.height(20.dp))
                    HorizontalDivider(thickness = 1.dp, color = Color.White.copy(0.05f))
                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "TELEMETRY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            color = Color.DarkGray,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(Modifier.height(16.dp))

                    // Pickups
                    HistoryMetricRow(
                        label = "Pickups",
                        value = item.pickups.toString(),
                        valueColor = if (item.pickups == 0) Color(0xFF34D399) else Color(0xFFEF4444)
                    )

                    Spacer(Modifier.height(10.dp))

                    // App Switches
                    HistoryMetricRow(
                        label = "App Switches",
                        value = item.appSwitches.toString(),
                        valueColor = if (item.appSwitches > 0) Color(0xFFF59E0B) else Color.White
                    )

                    Spacer(Modifier.height(10.dp))

                    // Score Impact
                    val prefix = if (item.scoreImpact >= 0) "+" else ""
                    val impactColor = if (item.scoreImpact >= 0) SoftIndigo else Color(0xFFEF4444)
                    HistoryMetricRow(
                        label = "Focus Score Impact",
                        value = "$prefix${item.scoreImpact} pts",
                        valueColor = impactColor
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryMetricRow(
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        )
    }
}