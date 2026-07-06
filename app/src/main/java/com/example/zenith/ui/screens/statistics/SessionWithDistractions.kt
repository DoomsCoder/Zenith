package com.example.zenith.ui.screens.statistics

import androidx.room.Embedded
import androidx.room.Relation
import com.example.zenith.data.DistractionEvent
import com.example.zenith.data.FocusSession

data class SessionWithDistractions(
    @Embedded val session: FocusSession,
    @Relation(
        parentColumn = "id",
        entityColumn = "sessionId"
    )
    val distractions: List<DistractionEvent>
)
