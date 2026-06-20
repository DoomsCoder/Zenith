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