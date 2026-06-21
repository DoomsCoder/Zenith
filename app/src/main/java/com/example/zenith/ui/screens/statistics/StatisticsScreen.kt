package com.example.zenith.ui.screens.statistics

import android.widget.Space
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen() {
    var showRules by remember { mutableStateOf(false) }
    var chartSelectedIndex by remember { mutableStateOf<Int?>(null) }
    val sheetState = rememberModalBottomSheetState()
    val dummySessions = listOf(
        SessionDummyData(
            id = 1,
            missionName = "AnkiDroid PR #20849",
            plannedDurationMinutes = 120,
            actualDurationMinutes = 120,
            isCompleted = true,
            date = "Jun 16, 3:05 PM",
            phonePickups = 0,
            appSwitches = 1,
            focusScoreImpact = 240
        ),
        SessionDummyData(
            id = 2,
            missionName = "OpenAI Buildathon Wealnex Setup",
            plannedDurationMinutes = 45,
            actualDurationMinutes = 14,
            isCompleted = false,
            date = "Jun 16, 4:20 PM",
            phonePickups = 3,
            appSwitches = 5,
            focusScoreImpact = -50
        )
    )

    val dummyChartMetrics = remember {
        val today = LocalDate.now()
        listOf(
            DailyFocusMetrics(today.minusDays(6), 30, 4),
            DailyFocusMetrics(today.minusDays(5), 60,5),
            DailyFocusMetrics(today.minusDays(4), 0,0),
            DailyFocusMetrics(today.minusDays(3), 90, 2),
            DailyFocusMetrics(today.minusDays(2), 120,2),
            DailyFocusMetrics(today.minusDays(1), 0,0),
            DailyFocusMetrics(today, 45, 1)
        )
    }

    // Using Box to provide the background color for the whole screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)) // DeepSlate Background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit){
                    detectTapGestures(
                        onTap = { chartSelectedIndex = null}
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
                        text = "June 16 2026",
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

            item {
                FocusScoreSection(isStreakLost = true, onShowRules = { showRules = true})
                Spacer(Modifier.height(48.dp))
                HorizontalDivider(color = Color.White.copy(0.05f))
                Spacer(Modifier.height(32.dp))
            }
            // MISSION LOG SECTION
            item {
                TodayMissionLogSection(
                    sessions = dummySessions,
                    onStartSessionClick = { /* Handle Start Session */ }
                )
                Spacer(Modifier.height(32.dp))
                HorizontalDivider(color = Color.White.copy(0.05f))
                Spacer(Modifier.height(24.dp))
            }

            item {
                ThisWeeksFocusChart(
                    metrics = dummyChartMetrics,
                    selectedColumnIndex = chartSelectedIndex,
                    onColumnSelected = {newIndex -> chartSelectedIndex = newIndex}
                    )
                Spacer(Modifier.height(32.dp))
            }

            item {
                val allTimeMetrics = AllTimeMetrics(
                    totalSessions = 47,
                    totalHours = 23.5f,
                    completionRate = 84,
                    bestStreak = 15
                )
                AllTelemetrySection(metrics = allTimeMetrics)
                Spacer(Modifier.height(48.dp))
            }
        }

        if (showRules) {
            ModalBottomSheet(
                onDismissRequest = { showRules = false},
                sheetState = sheetState,
                containerColor = Color(0xFF121212),
                dragHandle = { BottomSheetDefaults.DragHandle(color = Color.White.copy(0.1f))}
            ) {
                EngineRulesContent { showRules = false }
            }
        }
    }
}