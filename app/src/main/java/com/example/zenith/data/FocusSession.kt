package com.example.zenith.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a single study session.
 * @Entity tells Room to create a table named "focus_sessions".
 */
@Entity(tableName = "focus_sessions")
data class FocusSession (
    // Auto-generate means Room will handle the ID numbers (1, 2, 3...) for us
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    // Zone 1 input: "What are you focusing on?"
    val missionName: String,

    // The target set by the user (e.g., 25, 50)
    val plannedDurationMinutes: Int,

    // How long they actually stayed in focus
    val actualDurationSeconds: Int,

    // True if they finished the timer, False if they quit early
    val isCompleted: Boolean,

    // Record when the session happened
    val timestamp: Long
)