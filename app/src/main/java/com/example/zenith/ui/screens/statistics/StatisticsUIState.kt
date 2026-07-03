package com.example.zenith.ui.screens.statistics

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

/**
 * Think of this as a "Snapshot" of the entire Statistics screen.
 * Every variable here matches a specific part of the UI you already built.
 */
@Parcelize
data class StatisticsUIState (
    // If true, we can show a loading spinner while the database is working
    val isLoading: Boolean = true,
    val totalScore: Int = 0,
    val currentTier: FocusTier = FocusTier.INITIALIZING,
    val tierProgress: Float = 0f,
    val weeklyDelta: Int = 0,
    val scoreBreakdown: ScoreBreakdown = ScoreBreakdown(),
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val isStreakLost: Boolean = false,
    val todaySessions: List<SessionHistoryItem> = emptyList(),
    val historySessions: List<SessionHistoryItem> = emptyList(),
    val weeklyChartData: List<DailyFocusMetrics> = emptyList(),
    val allTimeMetrics: AllTimeMetrics = AllTimeMetrics(0, 0f, 0 ,0)
) : Parcelable


@Parcelize
data class SessionHistoryItem(
    val id: String,
    val dataTimeStr: String, // Matches your UI exactly
    val title: String,       // Mission Name
    val durationMinutes: Int, // Actual Minutes focused
    val plannedMinutes: Int,  // Needed for Today's Log expansion
    val isCompleted: Boolean,
    val pickups: Int,
    val appSwitches: Int,
    val scoreImpact: Int
): Parcelable

@Parcelize
data class DailyFocusMetrics(
    val date: LocalDate,
    val totalMinutes: Int,
    val sessionCount: Int
): Parcelable

@Parcelize
data class AllTimeMetrics(
    val totalSessions: Int,
    val totalHours: Float,
    val completionRate: Int,
    val bestStreak: Int
): Parcelable