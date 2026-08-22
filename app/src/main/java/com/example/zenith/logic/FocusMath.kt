package com.example.zenith.logic

object FocusMath {

    fun calculateTimeDebt(distractionSeconds: Int): Int {
        return distractionSeconds * 2
    }

    fun calculateBreakBank(missionMins: Int, isRelaxed: Boolean): Int {
        return if (isRelaxed) {
            (missionMins * 0.2).toInt().coerceAtLeast(2)
        } else {
            (missionMins * 0.1).toInt().coerceAtLeast(1)
        }
    }
}
