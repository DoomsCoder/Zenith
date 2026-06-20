package com.example.zenith.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/**
 * Represents a single "oops" moment (like a phone pickup).
 * linked to a FocusSession via its ID.
 */
@Entity(
    tableName = "distraction_events",
    foreignKeys = [
        ForeignKey(
            entity = FocusSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE // If a session is deleted, delete its events too
        )
    ]
    )
data class DistractionEvent(
    @PrimaryKey(autoGenerate = true)
    val id : Int = 0,

    val sessionId : Int,// This links the event to a specific session
    val timeStamp : Long,
    val distractionType : String // e.g., "PICKUP" or "SCREEN_UNLOCK"
)

