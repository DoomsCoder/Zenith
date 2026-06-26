package com.example.zenith.ui.screens.statistics

import com.example.zenith.data.FocusSession
import kotlin.math.max

/**
 * Data model representing the itemized point values for the Stats Screen breakdown.
 */
data class ScoreBreakdown(
    val completionPoints: Int = 0,
    val focusMinutePoints: Int = 0,
    val abandonmentPenalty: Int = 0,
    val distractionPenalty: Int = 0,
    val streakBonus: Int = 0,
    val totalScore: Int = 0,
)

/**
 * Zenith Tier System - Defines the thresholds for user progression.
 */
enum class FocusTier(val label: String, val minScore: Int) {
    INITIALIZING("INITIALIZING", 0),
    BUILDING_FOCUS("BUILDING FOCUS", 500),
    DEEP_WORKER("DEEP WORKER", 1500),
    FLOW_STATE("FLOW STATE", 3000),
    ZENITH_ACHIEVED("ZENITH ACHIEVED", 5000);

    fun next(): FocusTier? {
        val entries = FocusTier.entries
        val nextIndex = ordinal + 1
        return if (nextIndex < entries.size) entries[nextIndex] else null
    }
}

/**
 * The Master Logic Engine for Zenith.
 * This class calculates points, tiers, and progress for the entire app.
 */
object FocusScoreEvaluator {

    // SCORING CONSTANTS
    private const val PTS_SESSION_COMPLETE = 100
    private const val PTS_SESSION_ABANDON = -50
    private const val PTS_PER_MINUTE = 2
    private const val PTS_PER_PICKUP = -10
    private const val PTS_PER_APP_SWITCH = -15
    private const val PTS_STREAK_BONUS = 200 // Bonus awarded for every 7 days of streak

    /**
     * Calculates the score for a single session immediately after it ends.
     * Used by FocusService and for the History list items.
     */
    fun calculateSessionScore(
        isCompleted: Boolean,
        durationSeconds: Int,
        pickups: Int,
        appSwitches: Int
    ): Int {
        var score = 0
        score += if (isCompleted) PTS_SESSION_COMPLETE else PTS_SESSION_ABANDON
        score += (durationSeconds / 60) * PTS_PER_MINUTE
        score += pickups * PTS_PER_PICKUP
        score += appSwitches * PTS_PER_APP_SWITCH
        return score
    }

    fun calculateGrandBreakdown(
        allSessions: List<FocusSession>,
        totalPickups: Int,
        totalSwitches: Int,
        streakDays: Int
    ): ScoreBreakdown {
        val completedCount = allSessions.count { it.isCompleted }
        val abandonedCount = allSessions.count { !it.isCompleted }
        val totalFocusSeconds = allSessions.sumOf { it.actualDurationSeconds.toLong() }

        val compPts = completedCount * PTS_SESSION_COMPLETE
        val minutePts = (totalFocusSeconds / 60).toInt() * PTS_PER_MINUTE
        val abandonPenalty = abandonedCount * PTS_SESSION_ABANDON
        val distPenalty = (totalPickups * PTS_PER_PICKUP) + (totalSwitches * PTS_PER_APP_SWITCH)
        val bonus = (streakDays / 7) * PTS_STREAK_BONUS

        val total = compPts + minutePts + abandonPenalty + distPenalty + bonus

        return ScoreBreakdown(
            completionPoints = compPts,
            focusMinutePoints = minutePts,
            abandonmentPenalty = abandonPenalty,
            distractionPenalty = distPenalty,
            streakBonus = bonus,
            totalScore = max(0, total) // Zenith scores never go below zero
        )
    }

    /**
     * Returns the user's current Tier based on their total score.
     */
    fun getTierForScore(totalScore: Int): FocusTier {
        val score = max(0, totalScore)
        return when {
            score >= FocusTier.ZENITH_ACHIEVED.minScore -> FocusTier.ZENITH_ACHIEVED
            score >= FocusTier.FLOW_STATE.minScore -> FocusTier.FLOW_STATE
            score >= FocusTier.DEEP_WORKER.minScore -> FocusTier.DEEP_WORKER
            score >= FocusTier.BUILDING_FOCUS.minScore -> FocusTier.BUILDING_FOCUS
            else -> FocusTier.INITIALIZING
        }
    }

    /**
     * Calculates percentage progress (0.0 to 1.0) towards the next tier.
     */
    fun getProgressToNextTier(totalScore: Int): Float {
        val currentTier = getTierForScore(totalScore)
        val nextTier = currentTier.next() ?: return 1.0f

        val currentMin = currentTier.minScore
        val targetMin = nextTier.minScore

        val progress = (totalScore - currentMin).toFloat() / (targetMin - currentMin).toFloat()
        return progress.coerceIn(0f, 1f)
    }
}