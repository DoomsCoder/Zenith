package com.example.zenith.ui.screens.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zenith.ui.theme.MutedGray
import com.example.zenith.ui.theme.SoftIndigo
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel,
    onNavigateToHistory: () -> Unit,
    onNavigateToFocus: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var showRules by remember { mutableStateOf(false) }
    var chartSelectedIndex by remember { mutableStateOf<Int?>(null) }
    val sheetState = rememberModalBottomSheetState()

    // Using Box to provide the background color for the whole screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)) // DeepSlate Background
    ) {
        if(uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SoftIndigo)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { chartSelectedIndex = null }
                        )
                    },
                contentPadding = PaddingValues(
                    start = 24.dp,
                    end = 24.dp,
                    top = 24.dp,
                    bottom = 100.dp // Extra bottom padding for the bottom nav bar
                )
            ) {
                // DATE HEADER
                item {
                    DynamicDateHeader()
                }

                item {
                    FocusScoreSection(
                        score = uiState.totalScore,
                        weeklyDelta = uiState.weeklyDelta,
                        currentStreak = uiState.currentStreak,
                        bestStreak = uiState.bestStreak,
                        isStreakLost = uiState.isStreakLost,
                        tierProgress = uiState.tierProgress,
                        currentTierLabel = uiState.currentTier.label,
                        breakdown = uiState.scoreBreakdown,
                        onRecoveryClick = onNavigateToFocus,
                        onShowRules = { showRules = true}
                    )
                    Spacer(Modifier.height(48.dp))
                    HorizontalDivider(color = Color.White.copy(0.05f))
                    Spacer(Modifier.height(32.dp))
                }
                // MISSION LOG SECTION
                item {
                    TodayMissionLogSection(
                        sessions = uiState.todaySessions,
                        onStartSessionClick = onNavigateToFocus
                    )
                    Spacer(Modifier.height(32.dp))
                    HorizontalDivider(color = Color.White.copy(0.05f))
                    Spacer(Modifier.height(24.dp))
                }

                item {
                    ThisWeeksFocusChart(
                        metrics = uiState.weeklyChartData,
                        selectedColumnIndex = chartSelectedIndex,
                        onColumnSelected = {newIndex -> chartSelectedIndex = newIndex}
                    )
                    Spacer(Modifier.height(32.dp))
                }

                item {
                    AllTelemetrySection(metrics = uiState.allTimeMetrics)
                    Spacer(Modifier.height(48.dp))
                }

                item {
                    RecentLogSection(
                        sessions = uiState.historySessions.take(3),
                        onViewAllSessionsClick = onNavigateToHistory
                    )
                    Spacer(Modifier.height(48.dp))
                }
            }
        }

        if (showRules) {
            ModalBottomSheet(
                onDismissRequest = { showRules = false},
                sheetState = sheetState,
                containerColor = Color(0xFF121212),
                dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(0.1f))}
            ) {
                EngineRulesContent(
                    currentTierLabel = uiState.currentTier.label,
                    onClose = { showRules = false }
                )
            }
        }
    }
}

@Composable
private fun DynamicDateHeader() {
    val today = LocalDate.now()
    val formattedDate = today.format(DateTimeFormatter.ofPattern("MMMM dd yyyy"))

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TODAY",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = MutedGray,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    fontFamily = FontFamily.Monospace
                )
            )
            Text(
                text = formattedDate, // Uses real system date
                style = MaterialTheme.typography.labelMedium.copy(
                    color = MutedGray.copy(alpha = 0.5f),
                    fontFamily = FontFamily.Monospace
                )
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
        Spacer(modifier = Modifier.height(20.dp))
    }
}