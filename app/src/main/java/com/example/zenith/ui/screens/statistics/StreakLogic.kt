package com.example.zenith.ui.screens.statistics

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Result model for streak calculations.
 */
data class StreakInfo(
    val currentStreak: Int,
    val bestStreak: Int,
    val isStreakLost: Boolean
)

object StreakLogic {

    /**
     * Takes a list of unique date strings (YYYY-MM-DD) from the database
     * and calculates the streak information.
     */
    fun calculateStreaks(dateStrings: List<String>): StreakInfo {
        if (dateStrings.isEmpty()) return StreakInfo(0,0,false)

        val dates = dateStrings.map { LocalDate.parse(it) }.sortedDescending()
        val today = LocalDate.now()

        val latestActiveDate = dates.first()
        val daysSinceLastActive = ChronoUnit.DAYS.between(latestActiveDate, today)

        val isStreakLost = daysSinceLastActive > 1

        return StreakInfo(
            currentStreak = (if (isStreakLost) 0 else countCurrentStreak(dates)),
            bestStreak = calculateMaxChain(dates),
            isStreakLost = isStreakLost
        )
    }
}

/**
 * Count backwards from the most recent date.
 */
private fun countCurrentStreak(dates: List<LocalDate>): Int {
    var current = 1
        for (i in 0 until dates.size - 1) {
            val diff = ChronoUnit.DAYS.between(dates[i + 1], dates[i])
            if (diff == 1L){
                current++
            } else {
                break
            }
        }
    return current
}

/**
 * Scans the entire history for the longest chain
 */
private fun calculateMaxChain(dates: List<LocalDate>): Int {
    if (dates.isEmpty()) return 0
    var maxStreak = 1
    var tempStreak = 1

    for (i in 0 until dates.size - 1) {
        val diff = ChronoUnit.DAYS.between(dates[i+1],dates[i])

        if (diff == 1L) {
            tempStreak++
        } else {
            maxStreak = maxOf(maxStreak, tempStreak)
            tempStreak = 1
        }
    }

    return maxOf(maxStreak, tempStreak)
}
