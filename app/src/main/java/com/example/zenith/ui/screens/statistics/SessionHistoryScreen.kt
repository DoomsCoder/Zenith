package com.example.zenith.ui.screens.statistics

import androidx.activity.compose.BackHandler
import androidx.annotation.ColorRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    val haptic = LocalHapticFeedback.current

    // Dummy Data
    var allSessions = remember {
        listOf(
            SessionHistoryItem("1", "JUN 21 • 5:50 PM", "AnkiDroid PR #20849", 25, true, 0, 1, 130),
            SessionHistoryItem("2", "JUN 21 • 3:15 PM", "Chapter 4 Reading", 50, false, 3, 5, -20),
            SessionHistoryItem("3", "JUN 20 • 9:00 AM", "System Architecture", 45, true, 0, 0, 200)
        )
    }

    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    val isSelectionMode by remember { derivedStateOf { selectedIds.isNotEmpty() } }
    var selectedFilter by rememberSaveable { mutableStateOf("All") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val filteredSessions = remember(selectedFilter){
        val list = when(selectedFilter) {
            "Completed" -> allSessions.filter { it.isCompleted }
            "Abandoned" -> allSessions.filter { !it.isCompleted }
            else -> allSessions
        }

        list.reversed()
    }

    BackHandler(enabled = isSelectionMode) {
        selectedIds = emptySet()
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
            Surface (
                onClick ={ if (isSelectionMode) selectedIds = emptySet() else onBackClick() },
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.03f),
                border = BorderStroke(1.dp,Color.White.copy(0.08f))
            ) {
                Box(modifier = Modifier.size(35.dp), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White.copy(0.5f)
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = "SESSION HISTORY",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = Color.White.copy(0.6f)
                )
            )
        }

        Spacer(Modifier.height(32.dp))

        // Filters Row
        Box(modifier = Modifier.padding(horizontal = 24.dp)) {
            AnimatedContent (
                targetState = isSelectionMode,
                transitionSpec = {
                    fadeIn() + slideInVertically() togetherWith fadeOut() + slideOutVertically()
                },
                label = "ActionBar"
            ) { selecting ->
                if (selecting) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {

                            Surface(
                                modifier = Modifier.height(36.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = SoftIndigo.copy(0.2f),
                                border = BorderStroke(1.dp, SoftIndigo.copy(0.5f))
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
                                    Text(
                                        text = "${selectedIds.size} SELECTED",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = SoftIndigo, fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            val isAllSelected = selectedIds.size == filteredSessions.size
                            Surface(
                                onClick = {
                                    selectedIds = if (isAllSelected) emptySet() else filteredSessions.map { it.id }.toSet()
                                },
                                modifier = Modifier.height(36.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White.copy(0.05f),
                                border = BorderStroke(1.dp, Color.White.copy(0.1f))
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
                                    Text(
                                        text = if (isAllSelected) "DESELECT ALL" else "SELECT ALL",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = Color.White.copy(0.7f), fontWeight = FontWeight.Medium
                                        )
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = { showDeleteDialog = true},
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(0.03f))
                                .border(1.dp, Color(0xFFEF5350).copy(0.08f), RoundedCornerShape(12.dp))
                        ) {
                            Icon(
                                Icons.Rounded.Delete,
                                null,
                                tint = Color(0xFFEF5350),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
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
                                    shape = RoundedCornerShape(16.dp)
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
                }
            }
        }

        Text(
            text = "HOLD A CARD TO SELECT · TAP TO EXPAND",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 12.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = Color.White.copy(0.15f),
                letterSpacing = 1.sp
            )
        )

        HorizontalDivider(thickness = 1.dp, color = Color.White.copy(0.05f))
        Spacer(Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(start = 24.dp, top = 8.dp, end = 24.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items = filteredSessions, key = { it.id }) { session ->
                SessionHistoryCard(
                    item = session,
                    isSelected = selectedIds.contains(session.id),
                    isSelectionMode = isSelectionMode,
                    onToggle = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        selectedIds = if (selectedIds.contains(session.id)) selectedIds - session.id else selectedIds + session.id
                    }
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = Color(0xFF1A1A1A),
            title = { Text("Delete Sessions", color = Color.White, fontFamily = FontFamily.Monospace) },
            text = { Text("Are you sure you want to delete ${selectedIds.size} session(s)? This action is irreversible.", color = Color.Gray) },
            confirmButton = {
                TextButton(onClick = {
                    allSessions = allSessions.filterNot { selectedIds.contains(it.id) }
                    selectedIds = emptySet()
                    showDeleteDialog = false
                }) {
                    Text("DELETE", color = Color(0xFFEF5350), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("CANCEL", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
private fun SessionHistoryCard(
    item: SessionHistoryItem,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onToggle: () -> Unit
) {
    var isExpanded by remember {mutableStateOf(false)}
    val scale by animateFloatAsState(if (isSelected) 1.02f else 1f, label = "scale")

    val getTelemetryColor = { count: Int ->
        when (count) {
            0 -> Color(0xFF4CAF50)      // Green
            in 1..2 -> Color(0xFFFFA726)   // Amber/Orange
            else -> Color(0xFFEF5350)           // Red
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .combinedClickable(
                onLongClick = onToggle,
                onClick = { if (isSelectionMode) onToggle() else isExpanded = !isExpanded },
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ).animateContentSize(),
        color = if (isSelected) SoftIndigo.copy(0.07f) else Color(0xFF141414), // Dark Charcoal
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) SoftIndigo.copy(0.65f) else Color.White.copy(alpha = 0.05f))
    ) {
        Box {
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
                            valueColor = getTelemetryColor(item.pickups)
                        )

                        Spacer(Modifier.height(10.dp))

                        // App Switches
                        HistoryMetricRow(
                            label = "App Switches",
                            value = item.appSwitches.toString(),
                            valueColor = getTelemetryColor(item.appSwitches)
                        )

                        Spacer(Modifier.height(10.dp))

                        // Score Impact
                        val prefix = if (item.scoreImpact >= 0) "+" else ""
                        val impactColor =
                            if (item.scoreImpact >= 0) SoftIndigo else Color(0xFFEF4444)
                        HistoryMetricRow(
                            label = "Focus Score Impact",
                            value = "$prefix${item.scoreImpact} pts",
                            valueColor = impactColor
                        )
                    }
                }
            }

            if (isSelectionMode) {
                Box(
                    modifier = Modifier
                    .padding(16.dp)
                    .size(24.dp)
                    .align(Alignment.TopEnd)
                    .background(if (isSelected) SoftIndigo else Color.Transparent, CircleShape)
                    .border(1.5.dp, if (isSelected) SoftIndigo else Color.White.copy(0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) Icon(Icons.Rounded.Check, null, tint = Color.White, modifier = Modifier.size(16.dp))
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