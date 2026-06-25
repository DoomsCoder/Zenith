package com.example.zenith.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {

    // 'Flow' allows the UI to automatically update whenever the data changes
    @Query("SELECT * FROM focus_sessions ORDER BY timestamp DESC")
    fun getAllSessions() : Flow<List<FocusSession>>

    // Fetch Sessions for the 7-day chart
    @Query("SELECT * FROM focus_sessions WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp ASC")
    fun getSessionsInDateRange(startTime: Long, endTime: Long): Flow<List<FocusSession>>

    // Count total sessions for All-time Telemetry
    @Query("SELECT COUNT(*) FROM focus_sessions")
    fun getTotalSessionCount(): Flow<Int>

    // Sum total focus time for All-Time Telemetry
    @Query("SELECT SUM(actualDurationSeconds) FROM focus_sessions WHERE isCompleted = 1")
    fun getTotalFocusTimeSeconds(): Flow<Long?>

    // Get active days for Streak calculation (Returns YYYY-MM-DD strings)
    @Query("SELECT DISTINCT(date(timestamp / 1000, 'unixepoch', 'localtime')) FROM focus_sessions WHERE isCompleted = 1 ORDER BY timestamp DESC")
    fun getActiveFocusDays(): Flow<List<String>>

    @Query("DElETE FROM focus_sessions WHERE id IN (:sessionIds)")
    suspend fun deleteSessionById(sessionIds: List<Int>)

    // 'suspend' ensures this runs in the background so the app doesn't freeze
    @Query("SELECT * FROM focus_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Int): FocusSession?

    @Query("SELECT * FROM focus_sessions ORDER BY timestamp DESC LIMIT 1")
    fun getLatestSession() : Flow<FocusSession?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSession): Long

    @Update
    suspend fun updateSession(session: FocusSession)

    @Delete
    suspend fun deleteSession(session: FocusSession)
}