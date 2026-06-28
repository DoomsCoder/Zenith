package com.example.zenith.ui.screens.statistics

import com.example.zenith.ui.screens.focus.SessionState

/**
 * Think of this as a "Snapshot" of the entire Statistics screen.
 * Every variable here matches a specific part of the UI you already built.
 */
data class StatisticsUIState (

    // 1. System State
    // If true, we can show a loading spinner while the database is working
    val isLoading: Boolean = true,

    // 2. Focus Score Section
    // Powers the big number, the Tier label, and the Progress bar
    val totalScore: Int = 0,
    val currentTier: FocusTier = FocusTier.INITIALIZING,
    val tierProgress: Float = 0f,
    val weeklyDelta: Int = 0,
    val scoreBreakdown: ScoreBreakdown = ScoreBreakdown(),

    // 3. Streak Module
    // Powers the "🔥 12 days" and "⚡ 15 days" cards
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val isStreakLost: Boolean = false,

    // 4. Today's Log & Weekly Chart
    // The lists that the UI will loop through to draw the cards and bars
    val todaySessions: List<SessionData> = emptyList(),
    val weeklyChartData: List<DailyFocusMetrics> = emptyList(),


    // 5. All-Time Telemetry
    // Powers the bottom vertical list (Total Sessions, Hours, etc.)
    val allTimeMetrics: AllTimeMetrics = AllTimeMetrics(0, 0f, 0 ,0)
)