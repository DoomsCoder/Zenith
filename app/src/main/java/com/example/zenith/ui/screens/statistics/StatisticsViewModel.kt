package com.example.zenith.ui.screens.statistics

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.zenith.data.AppDatabase
import com.example.zenith.data.FocusSession
import com.example.zenith.ui.common.UiStateMachine
import com.example.zenith.ui.common.asUiStateMachine
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class StatisticsViewModel(
    application: Application,
    savedState: SavedStateHandle
) : AndroidViewModel(application) {
    private val uiStateMachine: UiStateMachine<StatisticsUIState> =
        savedState.asUiStateMachine(StatisticsUIState())

    val uiState: StateFlow<StatisticsUIState> by uiStateMachine

    private val db = AppDatabase.getDatabase(application)
    private val sessionDao = db.focusSessionDao()
    private val distractionDao = db.distractionEventDao()

    init {
        observeDatabase()
    }

    fun deleteSessions(ids: List<Int>) {
        viewModelScope.launch {
            sessionDao.deleteSessionById(ids)
        }
    }

    private fun observeDatabase() {
        viewModelScope.launch {

            combine(
                sessionDao.getAllSessions(),
                sessionDao.getActiveFocusDays(),
                distractionDao.getTotalPickupCounts(),
                distractionDao.getTotalAppSwitchesCount()
            ) { allSessions, activeDays, totalPickups, totalSwitches ->

                val breakdown = FocusScoreEvaluator.calculateGrandBreakdown(
                    allSessions = allSessions,
                    totalPickups = totalPickups,
                    totalSwitches = totalSwitches,
                    streakDays = activeDays.size
                )

                val streakInfo = StreakLogic.calculateStreaks(activeDays)

                val chartData = prepareChartData(allSessions)

                val today = LocalDate.now()
                val sessionDataList = allSessions.map { it.toSessionData() }
                val todaySessions = sessionDataList.filter {
                    it.dataTimeStr.contains(today.format(DateTimeFormatter.ofPattern("MMM dd")))
                }

                val totalFocusSeconds = allSessions.sumOf { it.actualDurationSeconds.toLong() }
                val completionRate = if (allSessions.isNotEmpty()) {
                    (allSessions.count { it.isCompleted } * 100) / allSessions.size
                } else 0

                val telemetry = AllTimeMetrics(
                    totalSessions = allSessions.size,
                    totalHours = totalFocusSeconds / 3600f,
                    completionRate = completionRate,
                    bestStreak = streakInfo.bestStreak
                )

                uiStateMachine.update {
                    copy(
                        isLoading = false,
                        totalScore = breakdown.totalScore,
                        currentTier = FocusScoreEvaluator.getTierForScore(breakdown.totalScore),
                        tierProgress = FocusScoreEvaluator.getProgressToNextTier(breakdown.totalScore),
                        scoreBreakdown = breakdown,
                        currentStreak = streakInfo.currentStreak,
                        bestStreak = streakInfo.bestStreak,
                        isStreakLost = streakInfo.isStreakLost,
                        todaySessions = todaySessions,
                        historySessions = sessionDataList,
                        weeklyChartData = chartData,
                        allTimeMetrics = telemetry
                    )
                }
            }.collect {}
        }
    }

    private fun prepareChartData(sessions: List<FocusSession>): List<DailyFocusMetrics> {
        val today = LocalDate.now()
        return (6 downTo 0).map { i ->
            val date = today.minusDays(i.toLong())
            val dataStr = date.toString()
            val dailySessions = sessions.filter {
                LocalDate.ofEpochDay(it.timestamp / 86400000).toString() == dataStr
            }
            DailyFocusMetrics(
                date = date,
                totalMinutes = dailySessions.sumOf { it.actualDurationSeconds } / 60,
                sessionCount = dailySessions.size
            )
        }
    }

    private fun FocusSession.toSessionData(): SessionHistoryItem {
        val pickupsCount = 0
        val switchesCount = 0

        val impact = FocusScoreEvaluator.calculateSessionScore(
            isCompleted = isCompleted,
            durationSeconds = actualDurationSeconds,
            pickups = pickupsCount,
            appSwitches = switchesCount
        )

        return SessionHistoryItem(
            id = id.toString(),
            dataTimeStr = java.time.Instant.ofEpochMilli(timestamp)
                .atZone(java.time.ZoneId.systemDefault())
                .format(java.time.format.DateTimeFormatter.ofPattern("MMM dd • h:mm a")),
            title = missionName,
            durationMinutes = actualDurationSeconds / 60,
            plannedMinutes = plannedDurationMinutes,
            isCompleted = isCompleted,
            pickups = pickupsCount,
            appSwitches = switchesCount,
            scoreImpact = impact
        )
    }
}