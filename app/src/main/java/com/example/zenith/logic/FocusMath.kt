package com.example.zenith.logic

/**
 * Single source of truth for the Focus Engine's mathematical rules.
 * Extracted for unit testing and system consistency.
 */
object FocusMath {

    /**
     * Calculates the time penalty (debt) based on distraction duration.
     * Rule: 2x the distraction time.
     */
    fun calculateTimeDebt(distractionSeconds: Int): Int {
        return distractionSeconds * 2
    }

    /**
     * Calculates the break allowance based on the planned mission length.
     * Rule:
     * - Standard: 10% of mission (Min 2m)
     * - Relaxed: 20% of mission (Min 5m)
     */
    fun calculateBreakBank(missionMins: Int, isRelaxed: Boolean): Int {
        return if (isRelaxed) {
            (missionMins * 0.2).toInt().coerceAtLeast(2)
        } else {
            (missionMins * 0.1).toInt().coerceAtLeast(1)
        }
    }
}
