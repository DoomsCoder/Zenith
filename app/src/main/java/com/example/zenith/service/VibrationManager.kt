package com.example.zenith.service

import android.os.VibrationEffect

object VibrationManager {
    
    // Pattern definition: array of (silent, vibrate, silent, vibrate...) in ms
    // We use -1 for amplitude to use the user-defined strength or 255 for fixed max
    
    val pulse = longArrayOf(0, 300, 200, 300)
    val alarm = longArrayOf(0, 100, 50, 100, 50, 100, 50, 100)
    val heartbeat = longArrayOf(0, 150, 100, 400)
    val sos = longArrayOf(0, 100, 100, 100, 100, 100, 300, 300, 300, 100, 100, 100, 100, 100)
    val heavyImpact = longArrayOf(0, 600, 100, 100, 100, 100)

    fun getPattern(index: Int): LongArray {
        return when(index) {
            0 -> pulse
            1 -> alarm
            2 -> heartbeat
            3 -> sos
            4 -> heavyImpact
            else -> pulse
        }
    }

    fun getPatternName(index: Int): String {
        return when(index) {
            0 -> "Pulse"
            1 -> "Alarm"
            2 -> "Heartbeat"
            3 -> "Morse SOS"
            4 -> "Heavy Impact"
            else -> "Pulse"
        }
    }
}
