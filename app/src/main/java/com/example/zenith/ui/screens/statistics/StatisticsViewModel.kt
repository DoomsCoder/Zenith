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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
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
                sessionDao.getSessionsWithDistraction(),
                sessionDao.getActiveFocusDays(),
                distractionDao.getTotalPickupCounts(),
                distractionDao.getTotalAppSwitchesCount()
            ) { sessionWithDistractions, activeDays, totalPickups, totalSwitches ->

                val historyItems = sessionWithDistractions.map { it.toUIItem() }
                val allSessions = sessionWithDistractions.map { it.session }

                val breakdown = FocusScoreEvaluator.calculateGrandBreakdown(
                    allSessions = allSessions,
                    totalPickups = totalPickups,
                    totalSwitches = totalSwitches,
                    streakDays = activeDays.size
                )

                val weeklyDelta = calculateWeeklyDelta(historyItems)
                val streakInfo = StreakLogic.calculateStreaks(activeDays)

                val chartData = prepareChartData(allSessions)
                val today = LocalDate.now()

                val todaySessions = historyItems.filter {
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
                        weeklyDelta = weeklyDelta,
                        currentTier = FocusScoreEvaluator.getTierForScore(breakdown.totalScore),
                        tierProgress = FocusScoreEvaluator.getProgressToNextTier(breakdown.totalScore),
                        scoreBreakdown = breakdown,
                        currentStreak = streakInfo.currentStreak,
                        bestStreak = streakInfo.bestStreak,
                        isStreakLost = streakInfo.isStreakLost,
                        todaySessions = todaySessions,
                        historySessions = historyItems,
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
            val dailySessions = sessions.filter { Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate() == date }
            DailyFocusMetrics(
                date.toEpochDay(),
                dailySessions.sumOf { it.actualDurationSeconds } / 60,
                dailySessions.size
            )
        }
    }

    private fun SessionWithDistractions.toUIItem(): SessionHistoryItem {
        val pCount = distractions.count { it.distractionType == "PICKUP" }
        val sCount = distractions.count { it.distractionType == "APP_SWITCH"}

        val impact = FocusScoreEvaluator.calculateSessionScore(
            isCompleted = session.isCompleted,
            durationSeconds = session.actualDurationSeconds,
            pickups = pCount,
            appSwitches = sCount
        )

        return SessionHistoryItem(
            id = session.id.toString(),
            dataTimeStr = Instant.ofEpochMilli(session.timestamp)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("MMM dd • h:mm a")),
            timestampIso = Instant.ofEpochMilli(session.timestamp).toString(),
            title = session.missionName,
            durationMinutes = session.actualDurationSeconds / 60,
            plannedMinutes = session.plannedDurationMinutes,
            isCompleted = session.isCompleted,
            pickups = pCount,
            appSwitches = sCount,
            scoreImpact = impact
        )
    }

    private fun calculateWeeklyDelta(items: List<SessionHistoryItem>): Int {
        val now = System.currentTimeMillis()
        val oneWeek = 7 * 24 * 60 * 60 * 1000L
        val thisWeekPoints = items.filter { (now - Instant.parse(it.timestampIso).toEpochMilli()) <= oneWeek }.sumOf { it.scoreImpact }
        val lastWeekPoints = items.filter {
            val age = now - Instant.parse(it.timestampIso).toEpochMilli()
            age > oneWeek && age <= (oneWeek * 2)
        }.sumOf { it.scoreImpact }
        return thisWeekPoints - lastWeekPoints
    }
}