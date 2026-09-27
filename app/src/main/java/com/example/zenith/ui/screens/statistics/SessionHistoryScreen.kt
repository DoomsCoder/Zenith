package com.example.zenith.ui.screens.statistics

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.zenith.ui.theme.MutedGray
import com.example.zenith.ui.theme.SoftIndigo

@Composable
fun SessionHistoryScreen(
    viewModel: StatisticsViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    val isSelectionMode by remember { derivedStateOf { selectedIds.isNotEmpty() } }
    var selectedFilter by rememberSaveable { mutableStateOf("All") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    val filteredSessions = remember(selectedFilter, uiState.historySessions){
        val list = when(selectedFilter) {
            "Completed" -> uiState.historySessions.filter { it.isCompleted }
            "Abandoned" -> uiState.historySessions.filter { !it.isCompleted }
            else -> uiState.historySessions
        }
        list.reversed()
    }

    BackHandler(enabled = isSelectionMode) {
        selectedIds = emptySet()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        Column(modifier = Modifier.background(Color(0xFF121212)).statusBarsPadding()) {
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { if (isSelectionMode) selectedIds = emptySet() else onBackClick() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Session history",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                )
            }
            Spacer(Modifier.height(8.dp))
        }

        Spacer(Modifier.height(32.dp))

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
                                shape = RoundedCornerShape(100.dp),
                                color = SoftIndigo.copy(0.12f),
                                border = BorderStroke(1.dp, SoftIndigo.copy(0.4f))
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 16.dp)) {
                                    Text(
                                        text = "${selectedIds.size} selected",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = SoftIndigo, fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            val isAllSelected = selectedIds.size == filteredSessions.size
                            TextButton(
                                onClick = {
                                    selectedIds = if (isAllSelected) emptySet() else filteredSessions.map { it.id }.toSet()
                                },
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text(
                                    text = if (isAllSelected) "Deselect all" else "Select all",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(0.7f))
                                )
                            }
                        }

                        IconButton(
                            onClick = { showDeleteDialog = true},
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Delete,
                                null,
                                tint = Color(0xFFEF5350),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                                        selectedContainerColor = SoftIndigo.copy(0.12f),
                                        labelColor = Color.Gray,
                                        selectedLabelColor = SoftIndigo
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        borderColor = Color.White.copy(0.1f),
                                        selectedBorderColor = SoftIndigo,
                                        borderWidth = 1.dp,
                                        selectedBorderWidth = 1.dp
                                    ),
                                    shape = RoundedCornerShape(100.dp)
                                )
                            }
                        }

                        Text(
                            text = "${filteredSessions.size} sessions",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.DarkGray)
                        )
                    }
                }
            }
        }

        Text(
            text = "Hold to select · Tap to expand",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 12.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                color = Color.White.copy(0.25f)
            )
        )

        HorizontalDivider(thickness = 0.5.dp, color = Color.White.copy(0.07f))
        Spacer(Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 24.dp, top = 8.dp, end = 24.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (filteredSessions.isEmpty()) {
                item {
                    HistoryEmptyState()
                }
            } else {
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
    }

    if (showDeleteDialog) {
        Dialog(onDismissRequest = { showDeleteDialog = false }) {
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
                        Icons.Rounded.Delete,
                        null,
                        tint = Color(0xFFEF5350),
                        modifier = Modifier.size(32.dp)
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        "Delete sessions",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Are you sure you want to delete ${selectedIds.size} session(s)? This action is irreversible.",
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(Modifier.height(32.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showDeleteDialog = false },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(100.dp),
                            border = BorderStroke(1.dp, Color.White.copy(0.12f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("Cancel", style = MaterialTheme.typography.labelLarge)
                        }

                        Button(
                            onClick = {
                                viewModel.deleteSessions(selectedIds.map { it.toInt() })
                                selectedIds = emptySet()
                                showDeleteDialog = false
                            },
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            shape = RoundedCornerShape(100.dp)
                        ) {
                            Text(
                                "Delete",
                                color = Color.White,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
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

    val successGreen = Color(0xFF4CAF50)
    val errorRed = Color(0xFFEF5350)

    val getTelemetryColor = { count: Int ->
        when (count) {
            0 -> successGreen
            in 1..2 -> Color(0xFFFFA726)
            else -> errorRed
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
        color = if (isSelected) SoftIndigo.copy(0.07f) else Color(0xFF141414),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) SoftIndigo.copy(0.65f) else Color.White.copy(alpha = 0.05f))
    ) {
        Box {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = item.dataTimeStr,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Spacer(Modifier.height(20.dp))

                HistoryMetricRow(
                    label = "Duration",
                    value = "${item.durationMinutes} min",
                    valueColor = Color.White
                )

                Spacer(Modifier.height(10.dp))

                val statusText = if (item.isCompleted) "Completed" else "Abandoned"
                val statusColor = if (item.isCompleted) successGreen else errorRed
                HistoryMetricRow(
                    label = "Status",
                    value = statusText,
                    valueColor = statusColor
                )

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
                            text = "Telemetry",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color.DarkGray,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Spacer(Modifier.height(16.dp))

                        HistoryMetricRow(
                            label = "Pickups",
                            value = item.pickups.toString(),
                            valueColor = getTelemetryColor(item.pickups)
                        )

                        Spacer(Modifier.height(10.dp))

                        HistoryMetricRow(
                            label = "App Switches",
                            value = item.appSwitches.toString(),
                            valueColor = getTelemetryColor(item.appSwitches)
                        )

                        Spacer(Modifier.height(10.dp))

                        val prefix = if (item.scoreImpact >= 0) "+" else ""
                        val impactColor = if (item.scoreImpact >= 0) successGreen else errorRed
                        HistoryMetricRow(
                            label = "Focus score impact",
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
private fun HistoryEmptyState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.History,
            null,
            tint = MutedGray.copy(0.2f),
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "No history records found.",
            color = MutedGray.copy(0.6f),
            style = MaterialTheme.typography.bodyLarge
        )
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
            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        )
    }
}
