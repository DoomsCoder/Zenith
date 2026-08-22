package com.example.zenith.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "focus_sessions")
data class FocusSession (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val missionName: String,
    val plannedDurationMinutes: Int,
    val actualDurationSeconds: Int,
    val isCompleted: Boolean,
    val timestamp: Long
)
