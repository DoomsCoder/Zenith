package com.example.zenith.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "distraction_events",
    foreignKeys = [
        ForeignKey(
            entity = FocusSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DistractionEvent(
    @PrimaryKey(autoGenerate = true)
    val id : Int = 0,
    val sessionId : Int,
    val timeStamp : Long,
    val distractionType : String
)
