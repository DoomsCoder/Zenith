package com.example.zenith.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DistractionEventDao {

    // Get all distractions for one specific session to show on a chart
    @Query("SELECT * FROM distraction_events WHERE sessionId = :sessionId")
    fun getEventForSession(sessionId: Int): Flow<List<DistractionEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(session: DistractionEvent)

    // Quickly count how many times the user was distracted
    @Query("SELECT COUNT(*) FROM distraction_events WHERE sessionId = :sessionId")
    fun getDistractionCount(sessionId: Int): Flow<Int>
}