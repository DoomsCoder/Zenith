package com.example.zenith

import com.example.zenith.ui.screens.statistics.FocusScoreEvaluator
import com.example.zenith.ui.screens.statistics.FocusTier
import org.junit.Test
import org.junit.Assert.assertEquals

class FocusScoreEvaluatorTest {

    @Test
    fun `calculateSessionScore with perfect 25 min session`() {
        val isCompleted = true
        val durationSeconds = 25*60
        val pickups = 0
        val appSwitches = 0

        val score = FocusScoreEvaluator.calculateSessionScore(
            isCompleted,durationSeconds,pickups,appSwitches
        )

        assertEquals(150, score)
    }

    @Test
    fun `calculateSessionScore with distractions applies penalties correctly`() {
        val isCompleted = true
        val durationSeconds = 10 * 60
        val pickups = 2      // -20 pts
        val appSwitches = 1  // -15 pts

        val score = FocusScoreEvaluator.calculateSessionScore(
            isCompleted, durationSeconds, pickups, appSwitches
        )

        assertEquals(85, score)
    }

    @Test
    fun `calculateSessionScore for abandoned session applies negative base`() {
        val isCompleted = false
        val durationSeconds = 30 * 60
        val pickups = 0
        val appSwitches = 0

        val score = FocusScoreEvaluator.calculateSessionScore(
            isCompleted, durationSeconds, pickups, appSwitches
        )

        assertEquals(10, score)
    }

    @Test
    fun `getTierForScore returns correct rank based on thresholds`() {
        // Initial tier
        assertEquals(FocusTier.INITIALIZING, FocusScoreEvaluator.getTierForScore(0))

        // Building Focus (500 pts)
        assertEquals(FocusTier.BUILDING_FOCUS, FocusScoreEvaluator.getTierForScore(500))

        assertEquals(FocusTier.DEEP_WORKER, FocusScoreEvaluator.getTierForScore(1600))

        // Flow State (3000 pts)
        assertEquals(FocusTier.FLOW_STATE, FocusScoreEvaluator.getTierForScore(3500))

        // Zenith Achieved (5000 pts)
        assertEquals(FocusTier.ZENITH_ACHIEVED, FocusScoreEvaluator.getTierForScore(6000))
    }
}